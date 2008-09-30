package uk.ac.warwick.util.content.texttransformers;

import org.jmock.MockObjectTestCase;

import uk.ac.warwick.util.content.cleaner.HtmlCleaner;

public final class BodyCleanupHtmlTransformerTest extends MockObjectTestCase {
    
    private BodyCleanupHtmlTransformer transformer;
    
    public void setUp() {
        transformer = new BodyCleanupHtmlTransformer(new HtmlCleaner());
    }
    
    public void testTransformJustBody() {
        String input = "<html><head>this is left alone <li></head>" +
                "<body>" +
                "<p>Test transform</p>" +
                "<p>Remove <u>underlining</u>" +
                "</body>" +
                "</html>";
        
        String result = transformer.transform(input);
        
        assertTrue(result.contains("<html><head>this is left alone <li></head>"));
        assertFalse(result.contains("<u>"));
        assertTrue(result.contains("<p>Test transform</p>\n"));
        assertTrue(result.contains("<p>Remove underlining</p>\n"));
        assertTrue(result.contains("</body>"));
    }
    
    public void testTransformAll() {
        String input = 
                "<p>Test transform <ul><li></ul></p>" +
                "<p>Remove <u>underlining</u>";
        
        String result = transformer.transform(input);
        
        assertFalse(result.contains("<u>"));
        assertFalse(result.contains("<html>"));
        assertFalse(result.contains("</body>"));
        
        assertTrue(result.contains("</li>"));
        assertTrue(result.contains("<p>Test transform"));        
    }
}
