package uk.ac.warwick.util.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCSVDocument<T> {

    private List<T> lines = new ArrayList<T>();
    
    private CSVLineWriter<T> writer;
    private CSVLineReader<T> reader;
    
    private List<String> headerFields = new ArrayList<String>();
    
    private boolean storeLines = true;
    
    private boolean headerLine;

    public AbstractCSVDocument(final CSVLineWriter<T> theWriter, final CSVLineReader<T> theReader) {
        this.writer = theWriter;
        this.reader = theReader;
    }

    public final void addLine(final T object) {
        lines.add(object);
    }

    public abstract void write(final Writer output) throws IOException;

    /**
     * This method should call reader.constructNewObject()
     * and then use reader.setColumn to give the reader the data
     * that the concrete class has read in.
     * @throws CSVException 
     */
    public abstract List<T> read(final Reader source) throws IOException, CSVException;
    
    public abstract int validate(final Reader source) throws IOException,CSVException;

    public final int getNumberOfRows() {
        return lines.size();
    }

    public final T getRow(final int i) {
        return lines.get(i);
    }


    public final void setStoreLines(final boolean storeLines) {
        this.storeLines = storeLines;
    }

    protected final List<T> getLines() {
        return lines;
    }

    protected final void setLines(final List<T> lines) {
        this.lines = lines;
    }

    protected final CSVLineReader<T> getReader() {
        return reader;
    }

    protected final void setReader(final CSVLineReader<T> reader) {
        this.reader = reader;
    }

    protected final CSVLineWriter<T> getWriter() {
        return writer;
    }

    protected final void setWriter(final CSVLineWriter<T> writer) {
        this.writer = writer;
    }

    protected final boolean isStoreLines() {
        return storeLines;
    }
    
    public final void addHeaderField(String headerField) {
        headerFields.add(headerField);
    }

    protected final List<String> getHeaderFields() {
        return headerFields;
    }

    protected final void setHeaderFields(List<String> headerFields) {
        this.headerFields = headerFields;
    }

    public final boolean isHeaderLine() {
        return headerLine;
    }

    public final void setHeaderLine(boolean printHeaderLine) {
        this.headerLine = printHeaderLine;
    }

}