package uk.ac.warwick.util.web.tags;

import junit.framework.TestCase;

public class MimeTypeNameTagTest extends TestCase {

    public void testExistantType() {
        MimeTypeNameTag tag = new MimeTypeNameTag();
        tag.setMimeType("text/plain");
        assertEquals("Text file", tag.getMimeTypeName());
    }
    
    public void testNonexistantType() {
        MimeTypeNameTag tag = new MimeTypeNameTag();
        String unrecognisedType = "app/x-unrecognised";
        tag.setMimeType(unrecognisedType);
        assertEquals(unrecognisedType, tag.getMimeTypeName());
    }

}
