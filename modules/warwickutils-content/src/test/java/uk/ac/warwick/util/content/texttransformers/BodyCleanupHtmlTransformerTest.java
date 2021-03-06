package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.cleaner.AbstractHtmlCleanerTest;

public final class BodyCleanupHtmlTransformerTest extends AbstractHtmlCleanerTest {
    
    private BodyCleanupHtmlTransformer transformer;
    
    @Before
    public void setUp() {
        transformer = new BodyCleanupHtmlTransformer(cleaner);
    }
    
    @Test
    public void transformJustBody() {
        String input = "<html><head>this is left alone <li></head>" +
                "<body>" +
                "<p>Test transform</p>" +
                "<p>Remove <u>underlining</u>" +
                "</body>" +
                "</html>";
        
        String result = transformer.apply(new MutableContent(null, input)).getContent();
        
        assertTrue(result.contains("<html><head>this is left alone <li></head>"));
        assertFalse(result.contains("<u>"));
        assertTrue(result.contains("<p>Test transform</p>\n"));
        assertTrue(result.contains("<p>Remove underlining</p>\n"));
        assertTrue(result.contains("</body>"));
    }
    
    @Test
    public void transformAll() {
        String input = 
                "<p>Test transform <ul><li></ul></p>" +
                "<p>Remove <u>underlining</u>";
        
        String result = transformer.apply(new MutableContent(null, input)).getContent();
        
        assertFalse(result.contains("<u>"));
        assertFalse(result.contains("<html>"));
        assertFalse(result.contains("</body>"));
        
        assertTrue(result.contains("</li>"));
        assertTrue(result.contains("<p>Test transform"));        
    }
}
