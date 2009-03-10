package uk.ac.warwick.util.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This class represents a CSV document.
 * 
 * This is the original version, which is considerably less good than the
 * alternate version, but I don't want to break HelpText so this is left
 * in for now for HelpText to use. There's no technical reason why it
 * couldn't use the new one though.
 *
 * @author xusqac
 */
public final class CSVDocument<T> extends AbstractCSVDocument<T> {
    
    public static final String DELIMETER = ",";
    public static final String FIELD_WRAPPER = "\"";
    public static final String NEW_LINE = System.getProperty("line.separator");
    
    private static final Logger LOGGER = Logger.getLogger(CSVDocument.class);
   
    public CSVDocument(final CSVLineWriter<T> theWriter, final CSVLineReader<T> theReader) {
        super(theWriter,theReader);
    }
    
    public void write(final Writer output) throws IOException {
        if (isHeaderLine() && !getHeaderFields().isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (int i=0;i < getHeaderFields().size(); i++) {
                sb.append(FIELD_WRAPPER);
                String field = getHeaderFields().get(i);
                if (field != null) {
                    sb.append(field);      // prevent null appearing
                }
                sb.append(FIELD_WRAPPER);
                if (i < getHeaderFields().size() -1) {
                    sb.append(DELIMETER);
                }
            }
            output.write(sb.toString());
            output.write(NEW_LINE);
        }
        
        for (T line: getLines()) {
            int numberOfCols = getWriter().getNoOfColumns(line);
            Assert.isTrue(numberOfCols > 0, "The number of columns must be greater than 0");

            StringBuffer sb = new StringBuffer();
            for (int i=0; i < numberOfCols; i++) {
                sb.append(FIELD_WRAPPER);
                String field = getWriter().getColumn(line, i);
                if (field != null) {
                    sb.append(field);      // prevent null appearing
                }
                sb.append(FIELD_WRAPPER);
                if (i < numberOfCols -1) {
                    sb.append(DELIMETER);
                }
            }
            output.write(sb.toString());
            output.write(NEW_LINE);
        }
    }

    public void read(final Reader source) throws IOException,CSVException {
        String line;
        BufferedReader br = new BufferedReader(source);
        while ((line = br.readLine()) != null) {
            String[] cols = parseCSVLine(line);
            if (cols.length == 0) {
                LOGGER.debug("skipping because of empty line.");
                continue;
            }
            T o = getReader().constructNewObject();
            for (int i=0; i<cols.length; i++) {
                getReader().setColumn(o, i, cols[i]);
            }
            getReader().end(o);
            if (isStoreLines()) {
                getLines().add(o);
            }
        }
        getReader().endData();
    }
    
    /**
     * Validate the CSV file. Returns the number of columns
     */
    public int validate(final Reader source) throws IOException,CSVException {
        String line;
        BufferedReader br = new BufferedReader(source);
        
        boolean first = true;
        int columns = 0;
        
        int lineNumber = 0;
        
        while ((line = br.readLine()) != null) {
            lineNumber++;
            
            String[] cols = parseCSVLine(line);
            if (cols.length == 0) {
                LOGGER.debug("skipping because of empty line.");
                continue;
            }
            
            if (first) {
                columns = cols.length;
            } else if (cols.length != columns) {
                throw new CSVException("Error on line " + lineNumber + " - found " + cols.length + " fields but expected " + columns);
            }
        }
        
        return columns;
    }
    
    /**
     * Parse the specified line into fields.  Each field will start and end with FIELD_WRAPPER.
     */
    private String[] parseCSVLine(final String theLine) {
        String s = theLine;
        if (!StringUtils.hasLength(s)) {
            //throw new IllegalStateException("csvLine cannot be null");
            return new String[]{};
        }

        List<String> fields = new ArrayList<String>();
        int endOfFieldMarker = determineEndOfField(s);
        while (true) {
            String field = s.substring(0, endOfFieldMarker);
            field = removeFieldWrappersIfNeccessary(field);

            fields.add(field.trim());

            if (endOfFieldMarker == s.length()) {
                break;
            }
            // skip the next ","
            s = s.substring(endOfFieldMarker + 1).trim();
            endOfFieldMarker = determineEndOfField(s);

        }

        return fields.toArray(new String[] {});
    }

    private static String removeFieldWrappersIfNeccessary(final String string) {
        if (!StringUtils.hasLength(string)) {
            return string;
        }

        String newString = string;
        if (newString.startsWith(FIELD_WRAPPER)) {
            newString = newString.substring(FIELD_WRAPPER.length());
        }

        if (newString.endsWith(FIELD_WRAPPER)) {
            newString = newString.substring(0, newString.length() - FIELD_WRAPPER.length());
        }
        return newString;
    }

    private int determineEndOfField(final String csvLine) {
        int endOfFieldMarker;
        if (csvLine.startsWith(FIELD_WRAPPER)) {
            // return the next occurence of FIELD_WRAPPER (ignore the first one)
            endOfFieldMarker = csvLine.indexOf(FIELD_WRAPPER, 1) + 1;
        } else {
            endOfFieldMarker = csvLine.indexOf(DELIMETER);
            // there may not be another one, so set to end of line if not found
            if (endOfFieldMarker == -1) {
                endOfFieldMarker = csvLine.length();
            }
        }
        return endOfFieldMarker;
    }
}
