package uk.ac.warwick.util.csv;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;
import uk.ac.warwick.util.core.StringUtils;

public final class GoodCsvReadingTests extends TestCase {
    
    //we can store this Latin-1 character, as long as we don't actually type it in the source code.
    private static final char POUND = (char)163;
    
    public void testNonAsciiCharacters() throws Exception {
        NamedValueCSVLineReader reader = new NamedValueCSVLineReader();
        reader.setHasHeaders(true);
        String lastValue = POUND+"Mon"+POUND+"y"+POUND;
        String input = "Product,Type,"+lastValue;

        GoodCsvDocument<List<String>> doc = new GoodCsvDocument<List<String>>(null, reader);
        doc.read(new StringReader(input));
        
        List<String> headers = reader.getHeaders();
        assertEquals(3, headers.size());
        assertEquals(lastValue, headers.get(2));
    }
    
    public void testNonAsciiCharactersFromAFile() throws Exception {
        NamedValueCSVLineReader reader = new NamedValueCSVLineReader();
        reader.setHasHeaders(true);
        String lastValue = POUND+"Mon"+POUND+"y"+POUND;

        InputStream stream = getClass().getResourceAsStream("/csvWithPound.csv");
        
        GoodCsvDocument<List<String>> doc = new GoodCsvDocument<List<String>>(null, reader);
        doc.read(new InputStreamReader(stream, StringUtils.DEFAULT_ENCODING));
        
        List<String> headers = reader.getHeaders();
        assertEquals(3, headers.size());
        assertEquals(lastValue, headers.get(2));
    }
    
    /**
     * Demonstrate that when using the wrong encoding, the pound signs don't
     * come out as expected.
     */
    public void testNonAsciiCharactersFromAFileWithWrongCharset() throws Exception {
        NamedValueCSVLineReader reader = new NamedValueCSVLineReader();
        reader.setHasHeaders(true);
        String lastValue = POUND+"Mon"+POUND+"y"+POUND;

        InputStream stream = getClass().getResourceAsStream("/csvWithPound.csv");
        
        GoodCsvDocument<List<String>> doc = new GoodCsvDocument<List<String>>(null, reader);
        doc.read(new InputStreamReader(stream, "utf-8"));
        
        List<String> headers = reader.getHeaders();
        assertEquals(3, headers.size());
        assertFalse(lastValue.equals(headers.get(2)));
    }
}
