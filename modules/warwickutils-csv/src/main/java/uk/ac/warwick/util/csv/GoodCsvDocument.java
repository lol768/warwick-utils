package uk.ac.warwick.util.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import uk.ac.warwick.util.core.StringUtils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.google.common.collect.Lists;

/**
 * A CSV document object which uses an external library and should
 * be pretty robust.
 * 
 * TODO implement write()
 * 
 * @author cusebr
 */
public final class GoodCsvDocument<T> extends AbstractCSVDocument<T> {
    
    public GoodCsvDocument(final CSVLineWriter<T> theWriter, final CSVLineReader<T> theReader) {
        super(theWriter, theReader);
    }

    /**
     * This is actually fairly similar to the one in CSVDocument,
     * other than the key bits where it gets a new row and
     * fetches the array of values. Template out the differences?
     * It's not a very long method though, it might just make it
     * confusing.
     */
    @Override
    public List<T> read(final Reader source) throws IOException,CSVException {
        List<T> readLines = Lists.newArrayList();
        
        CsvReader delegate = new CsvReader(source);
        while (delegate.readRecord()) {
            if (delegate.getColumnCount() == 0) {
                continue;
            }
            T o = getReader().constructNewObject();
            String[] values = delegate.getValues();
            
            // SBTWO-4753 Ignore blank rows
            boolean allBlank = true;
            for (int i=0, length=values.length; i<length; i++) {
                if (StringUtils.hasText(values[i])) {
                    allBlank = false;
                }
            }
            if (allBlank) {
                continue;
            }
            
            for (int i=0; i<values.length; i++) {
                getReader().setColumn(o, i, values[i]);
            }
            getReader().end(o);
            
            readLines.add(o);
        }
        getReader().endData();
        
        if (isStoreLines()) {
            getLines().addAll(readLines);
        }
        
        return readLines;
    }
    
    /**
     * Validate the CSV file. Returns the number of columns
     */
    @Override
    public int validate(final Reader source) throws IOException,CSVException {        
        boolean first = true;
        int columns = 0;
        
        int lineNumber = 0;
        
        CsvReader delegate = new CsvReader(source);
        while (delegate.readRecord()) {
            lineNumber++;
            
            if (delegate.getColumnCount() == 0) {
                continue;
            }
            
            if (first) {
                columns = delegate.getColumnCount();
            } else if (delegate.getColumnCount() != columns) {
                throw new CSVException("Error on line " + lineNumber + " - found " + delegate.getColumnCount() + " fields but expected " + columns);
            }
        }
        
        return columns;
    }

    /**
     * TODO implement using CsvWriter, quite simple
     */
    @Override
    public void write(final Writer output) throws IOException {
        CsvWriter writer = new CsvWriter(output, ',');
        writer.setForceQualifier(true); // enforce identical behaviour to CSVDocument
        
        if (isHeaderLine() && !getHeaderFields().isEmpty()) {
            String[] record = new String[getHeaderFields().size()];
            
            for (int i=0;i < getHeaderFields().size(); i++) {
                record[i] = getHeaderFields().get(i);
            }
            
            writer.writeRecord(record);
        }
        
        for (T line: getLines()) {
            int numberOfCols = getWriter().getNoOfColumns(line);
            if (numberOfCols == 0) {
                throw new IllegalStateException("The number of columns must be greater than 0");
            }
            
            String[] record = new String[numberOfCols];
            
            for (int i=0;i < numberOfCols; i++) {
                record[i] = getWriter().getColumn(line, i);
            }
            
            writer.writeRecord(record);
        }
    }

}
