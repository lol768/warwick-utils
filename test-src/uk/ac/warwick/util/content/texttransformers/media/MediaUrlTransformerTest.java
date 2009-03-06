package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class MediaUrlTransformerTest extends TestCase {
    public void testTagMatchingPatternWorks() {
        Pattern p = new MediaUrlTransformer(null).getTagPattern();
        String input = "Test words [media]Contents[/media] More words";
        Matcher m = p.matcher(input);
        assertTrue(m.find());
        assertEquals("Contents", m.group(3));
    }
    
    public void testTagMatchingPatternWorksOnMultipleTags() {
        Pattern p = new MediaUrlTransformer(null).getTagPattern();
        String input = "Test words [media]Contents[/media] More words [media]Contents 2[/media] More words";
        Matcher m = p.matcher(input);
        assertTrue(m.find());
        assertEquals("Contents", m.group(3));
        assertTrue(m.find());
        assertEquals("Contents 2", m.group(3));
    }
    
    public void testTransforming() {
        Map<String,MediaUrlHandler> handlers = new HashMap<String,MediaUrlHandler>();
        handlers.put("uppercase", new MediaUrlHandler() {
            public boolean recognises(String url) {
                return true;
            }
            public String getHtml(String url, Map<String, Object> parameters) {
                return url.toUpperCase();
            }
        });
        MediaUrlTransformer transformer = new MediaUrlTransformer(handlers);
        
        String input = "test words [media]golden trumpet[/media] test words";
        String expected = "test words GOLDEN TRUMPET test words";
        String output = transformer.transform(input);
        
        assertEquals(expected, output);   
    }
    
    public void testParameters() {
        Map<String,MediaUrlHandler> handlers = new HashMap<String,MediaUrlHandler>();
        handlers.put("uppercase", new MediaUrlHandler() {
            public boolean recognises(String url) {
                return true;
            }
            public String getHtml(String url, Map<String, Object> parameters) {
                String result = url.toUpperCase() + "(";
                result += "width:"+parameters.get("width")+",";
                result += "height:"+parameters.get("height")+",";
                result += "type:"+parameters.get("type");
                result += ")";
                return result;
            }
        });
        MediaUrlTransformer transformer = new MediaUrlTransformer(handlers);
        
        String input = "test words [media width=100 height=\"200\" type='uppercase']trumpet[/media] test words";
        String expected = "test words TRUMPET(width:100,height:200,type:uppercase) test words";
        String output = transformer.transform(input);
        assertEquals(expected, output);        
    }
    
    /**
     * This test has two URL handlers, only one of which is set to recognise anything.
     * But if a type parameter is handed to the media URL it should override all this
     * and use the specified handler.
     */
    public void testContentTypeOverride() {
        Map<String,MediaUrlHandler> handlers = new HashMap<String,MediaUrlHandler>();
        handlers.put("uppercase", new MediaUrlHandler() {
            public boolean recognises(String url) {
                return true;
            }
            public String getHtml(String url, Map<String, Object> parameters) {
                return url.toUpperCase();
            }
        });
        handlers.put("lowercase", new MediaUrlHandler() {
            public boolean recognises(String url) {
                return false;
            }
            public String getHtml(String url, Map<String, Object> parameters) {
                return url.toLowerCase();
            }
        });
        MediaUrlTransformer transformer = new MediaUrlTransformer(handlers);
        
        String input = "test words [media]golden trumpet[/media] test words [media type='lowercase']Super LowerCase[/media] test words";
        String expected = "test words GOLDEN TRUMPET test words super lowercase test words";
        String output = transformer.transform(input);
        
        assertEquals(expected, output);
    }
    
    /**
     * Test that if the contentType is nonexistant or is a totally wrongmatch for the
     * given URL, that the handler gracefully leaves the [media] tag alone. We don't
     * want to throw an exception as it's feasibly something users could do, best not
     * to chuck up errors. 
     */
    public void testBadContentType() {
        Map<String,MediaUrlHandler> handlers = new HashMap<String,MediaUrlHandler>();
        handlers.put("uppercase", new MediaUrlHandler() {
            public boolean recognises(String url) {
                return true;
            }
            public String getHtml(String url, Map<String, Object> parameters) {
                return url.toUpperCase();
            }
        });
        
        MediaUrlTransformer transformer = new MediaUrlTransformer(handlers);
        String input = "test words [media type='somemadeuptype']golden trumpet[/media] test words";
        String output = transformer.transform(input);
        assertEquals(input, output);
    }
}