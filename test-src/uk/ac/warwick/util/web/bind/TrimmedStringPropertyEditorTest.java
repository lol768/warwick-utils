package uk.ac.warwick.util.web.bind;

import junit.framework.TestCase;

public class TrimmedStringPropertyEditorTest extends TestCase {

    TrimmedStringPropertyEditor editor;
    
    public void setUp() {
        editor = new TrimmedStringPropertyEditor();
    }
    
    public void testNull() {
        editor.setAsText(null);
        assertEquals("", editor.getAsText());
    }
    
    public void testNeverSet() {
        assertEquals("", editor.getAsText());
    }
    
    public void testSetValueThenGetText() {
        editor.setValue("Hello  ");
        assertEquals("Hello", editor.getAsText());
    }

}
