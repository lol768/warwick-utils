package uk.ac.warwick.util.web.bind;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.util.web.Uri;

public final class UriPropertyEditorTest {
    
    private final UriPropertyEditor editor = new UriPropertyEditor();
    
    @Test
    public void empty() {
        editor.setAsText("");
        assertNull(editor.getValue());
        assertNull(editor.getAsText());
    }
    
    @Test
    public void nullContent() {
        editor.setAsText(null);
        assertNull(editor.getValue());
        assertNull(editor.getAsText());
    }
    
    @Test
    public void itWorks() {
        editor.setAsText("http://www.warwick.ac.uk");
        assertEquals(Uri.parse("http://www.warwick.ac.uk"), editor.getValue());
        assertEquals("http://www.warwick.ac.uk", editor.getAsText());
    }

}
