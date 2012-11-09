package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public final class StringUtilsSafeSubstringTest extends TestCase {
    public void testOKParams() {
        String s = "abcdef";
        int start = 0;
        int end = 2;
        assertEquals(s.substring(start, end), StringUtils.safeSubstring(s, start, end));
    }

    public void testNegativeStart() {
        String s = "ssss";
        assertEquals(s, StringUtils.safeSubstring(s, -1, s.length()));
    }

    public void testEndBiggerThanString() {
        String s = "ssss";
        assertEquals(s, StringUtils.safeSubstring(s, 0, s.length() + 2));
    }

    public void testNegativeStartAndEnd() {
        assertEquals("", StringUtils.safeSubstring("asdvsdfv", -3, -1));
    }

    public void testStartBiggerThanEnd() {
        assertEquals("", StringUtils.safeSubstring("asdvsdfv", 2, 1));
    }

    public void testNullString() {
        assertEquals("", StringUtils.safeSubstring(null, 0, 1));
    }
}
