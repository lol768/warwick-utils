package uk.ac.warwick.util.csv;

import java.util.List;

import org.junit.Test;

public class CSVLineReaderTest {
    
    @Test public void overlyLongDataToNamedValueReader() throws Exception {
        NamedValueCSVLineReader reader = new NamedValueCSVLineReader();
        reader.setHasHeaders(true);
        supplyDataLongerThanHeaderTo(reader);
    }
    
    /**
     * Check that if a row has more values than there are headers, the
     * reader does not throw an exception.
     */
    public void supplyDataLongerThanHeaderTo(CSVLineReader<List<String>> reader) throws Exception {
        List<String> object = reader.constructNewObject();
        reader.setColumn(object, 0, "Header1");
        reader.setColumn(object, 1, "Header2");
        reader.setColumn(object, 2, "Header3");
        reader.end(object);
        
        object = reader.constructNewObject();
        reader.setColumn(object, 0, "Data1");
        reader.setColumn(object, 1, "Data2");
        reader.setColumn(object, 2, "Data3");
        reader.setColumn(object, 3, "Data4");
        reader.end(object);
    }
}
