package uk.ac.warwick.util.content.texttransformers;

import junit.framework.TestCase;

public final class NewWindowLinkTextTransformerTest extends TestCase {

    String image = NewWindowLinkTextTransformer.HTML_IMAGE;
    
    /**
     * Check that the transform is reconstructing the input correctly, by having
     * a transform not alter any tags.
     */
    public void testTagTransformerDoNothing() {
        String input = "Test abc <a href='abc' target=\"_blank\">Link</a> xyz";
        TagTransformer transformer = new TagTransformer("a");
        TextPatternTransformer.Callback callback = new TextPatternTransformer.Callback() {
            public String transform(final String i) {
                return i;
            }
        };
        String result = transformer.transform(input, callback);
        
        assertEquals(input, result);
    }
    
    public void testTagTransformerReplaceText() {
        String input = "Test abc <a href='abc' target=\"_blank\">Link</a> xyz";
        String expected = "Test abc REPLACE xyz";
        TagTransformer transformer = new TagTransformer("a");
        String result = transformer.transform(input, new TextPatternTransformer.Callback() {
            public String transform(final String i) {
                return "REPLACE";
            }        
        });
        
        assertEquals(expected, result);
    }
    
    public void testTagTransformerSurroundText() {
        String input = "Test abc <a href='abc' target=\"_blank\">Link</a> xyz";
        String expected = "Test abc START<a href='abc' target=\"_blank\">Link</a>END xyz";
        TagTransformer transformer = new TagTransformer("a");
        String result = transformer.transform(input, new TextPatternTransformer.Callback() {
            public String transform(final String i) {
                return "START" + i + "END";
            }        
        });
        
        assertEquals(expected, result);
    }
    
    public void testTagTransformerUppercase() {
        String input = "Test abc <a href='abc' target=\"_blank\">Link</a> xyz";
        String expected = "Test abc <A HREF='ABC' TARGET=\"_BLANK\">LINK</A> xyz";
        TagTransformer transformer = new TagTransformer("a");
        String result = transformer.transform(input, new TextPatternTransformer.Callback() {
            public String transform(final String i) {
                return i.toUpperCase();
            }        
        });
        
        assertEquals(expected, result);
    }
    
    public void testBasicRewrite() {        
        String input = "Text text <a href=\"something\" target=\"_blank\">Link</a>";
        String expected = "Text text <a href=\"something\" target=\"_blank\">Link"+image+"</a>";
        
        verify(input, expected);
    }
    
    public void testTwoLinks() {        
        String input = "Text text <a href=\"something\" target=\"_blank\">Link</a> and <a href=\"somethingelse\" target=\"_blank\">Another Link</a>";
        String expected = "Text text <a href=\"something\" target=\"_blank\">Link"+image+"</a> and <a href=\"somethingelse\" target=\"_blank\">Another Link"+image+"</a>";
        
        verify(input, expected);
    }
    
    public void testSingleQuotes() {        
        String input = "Text text <a href='something' target='_blank'>Link</a>";
        String expected = "Text text <a href='something' target='_blank'>Link"+image+"</a>";
        
        verify(input, expected);
    }
    
    public void testTagsInLink() {
        String input = "Text text <a href='something' target='_blank'>Link with <strong>tags</strong> in it</a>";
        String expected = "Text text <a href='something' target='_blank'>Link with <strong>tags</strong> in it"+image+"</a>";
        
        verify(input, expected);
    }
    
    /**
     * To reproduce example given in SBTWO-685
     */
    public void testParticularTagsInLink() {
        String input = "<a href=\"http://www2.warwick.ac.uk\" target=\"_blank\"><strong>Warwick home page</strong></a>";
        String expected = "<a href=\"http://www2.warwick.ac.uk\" target=\"_blank\"><strong>Warwick home page</strong>"+image+"</a>";
        
        verify(input, expected);
    }
    
    public void testNewlinesInCaption() {
        String input    = "<a href=\"http://www2.warwick.ac.uk\" target=\"_blank\">Warwick \nhome page</a>";
        String expected = "<a href=\"http://www2.warwick.ac.uk\" target=\"_blank\">Warwick \nhome page"+image+"</a>";
        
        verify(input, expected);
    }
    
    public void testNewLinesInTag() {
        String input    = "<a href=\"http://www2.warwick.ac.uk\" \n target=\"_blank\">Warwick home page</a>";
        String expected = "<a href=\"http://www2.warwick.ac.uk\" \n target=\"_blank\">Warwick home page"+image+"</a>";
        
        verify(input, expected);
    }
    
    public void testVaryCaseTags() {  
        String input = "Text text <A Href=\"something\" TARGET=\"_Blank\">Link</a>";
        String expected = "Text text <A Href=\"something\" TARGET=\"_Blank\">Link"+image+"</a>";
        
        verify(input, expected);
    }
    
//    public void testRemovesClassFromLinkThatOpensInSameWindow() {
//        String input = "Text text <a href=\"something\" class=\""+cssClass+"\">Link</a> ";
//        String expected = "Text text <a href=\"something\" class=\"\">Link</a> ";
//        
//        verify(input, expected); 
//    }
//    
//    public void testRemovesClassFromLinkThatOpensInSameWindowWithExistingClass() {
//        String input = "Text text <a href=\"something\" class=\"fish "+cssClass+"\">Link</a> ";
//        String expected = "Text text <a href=\"something\" class=\"fish\">Link</a> ";
//        
//        verify(input, expected); 
//    }
    
    public void testEmptyLink() {
        String input = "Text test <a>Blah</a>";
        verify (input, input);
    }
    
    private void verify(final String input, final String expected) {
        NewWindowLinkTextTransformer parser = new NewWindowLinkTextTransformer();
        assertEquals(expected, parser.transform(input));
    }

}
