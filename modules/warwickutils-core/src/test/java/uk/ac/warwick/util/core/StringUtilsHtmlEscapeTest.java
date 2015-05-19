package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public class StringUtilsHtmlEscapeTest extends TestCase {
    public void testNothingEscaped() {
        String input = "<p>Test input, no special characters</p>";
        verify(input,input);
    }
    
    public void testItWorks() {
        String input = "<p>Test high chars "+String.valueOf((char)147) +
                        "hello"+String.valueOf((char)148)+"</p>";
        String expected = "<p>Test high chars &#147;hello&#148;</p>";
        verify(input,expected);
    }

    public void testAstralPlanes() {
        String input = "Walking man: \ud83d\udeb6";
        String expected = "Walking man: &#128694;";
        verify(input,expected);
    }
    
    private void verify(String input, String expected) {
        assertEquals(expected, StringUtils.htmlEscapeHighCharacters(input));
    }
}
