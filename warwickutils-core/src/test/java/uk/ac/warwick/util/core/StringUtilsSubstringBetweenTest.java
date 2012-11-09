package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public class StringUtilsSubstringBetweenTest extends TestCase {
    public void testSubstringFound() {
        String input = "abcdefghijklmn";
        String start = "def";
        String end = "kl";
        String expected = "ghij";
        
        assertEquals(expected, StringUtils.substringBetween(input, start, end));
    }
    
    public void testIfMultipleEndStringsUseTheFirst() {
        String input = "abcdQVCendieuriendjiji";
        String start = "abcd";
        String end = "end";
        String expected = "QVC";
        
        assertEquals(expected, StringUtils.substringBetween(input, start, end));
    }
    
    public void testRestOfStringReturnedIfEndIsMissing() {
        String input = "abcdQVCenieurindjiji";
        String start = "abcd";
        String end = "end";
        String expected = "QVCenieurindjiji";
        
        assertEquals(expected, StringUtils.substringBetween(input, start, end));
    }
}
