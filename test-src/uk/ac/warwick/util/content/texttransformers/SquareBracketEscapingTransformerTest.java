package uk.ac.warwick.util.content.texttransformers;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.MutableContent;

public class SquareBracketEscapingTransformerTest extends TestCase {
    
    private static final String OPEN  = "&#91;";
    private static final String CLOSE = "&#93;";
    
    public void testMediaTagsAreEscaped() {
        String input = "Test [media crab=blah]url[/media] frank";
        String expected = "Test "+OPEN+"media crab=blah"+CLOSE+"url"+OPEN+"/media"+CLOSE+" frank";
        verify(input, expected);
    }
    
    public void testFootnotesAreNotEscaped() {
        String input = "Test some text with a footnote[1] which should be left alone";
        verify(input, input);
    }
    
    public void testNamedLinksAreNotEscaped() {
        String input = "Test this \"Link\":testlink \n\n [testlink]http://url.com/ ";
        verify(input, input);
    }

    /**
     * Verify that the input is pretransformed into the expected string, and also
     * that it is posttransformed back to the original input string.
     */
    private void verify(String input, String expected) {
        SquareBracketEscapingTransformer trans = new SquareBracketEscapingTransformer(new DoNothingTextTransformer());
        String text = input;
        text = trans.preTransform(new MutableContent(null, text)).getContent();
        assertEquals(expected, text);
        text = trans.postTransform(new MutableContent(null, text)).getContent();
        assertEquals(input, text);
    }
}
