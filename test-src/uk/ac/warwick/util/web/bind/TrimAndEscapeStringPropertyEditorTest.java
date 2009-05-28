package uk.ac.warwick.util.web.bind;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TrimAndEscapeStringPropertyEditorTest {
    
    TrimAndEscapeStringPropertyEditor e;
    
    @Before
    public void setUp() throws Exception {
        e = new TrimAndEscapeStringPropertyEditor();
    }
    
    @Test public void nullText() {
        e.setAsText(null);
        assertEquals("", e.getAsText());
    }
    
    @Test public void neverSet() {
        assertEquals("", e.getAsText());
    }
    
    @Test public void strangeWindowsCharacters() {
        String expected = "Doctor&#146;s Hours &amp; Towers";
        String input = "Doctor"+(char)146+"s Hours &amp; Towers";
        e.setAsText(input);
        assertEquals(expected, e.getAsText());
        assertEquals(expected, e.getValue());
    }

}
