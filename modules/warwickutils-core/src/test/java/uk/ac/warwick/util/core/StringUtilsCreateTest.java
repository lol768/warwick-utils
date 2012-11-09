package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public final class StringUtilsCreateTest extends TestCase {
    public void testItWorks() {
        String s = "the string";

        assertEquals(s, StringUtils.create(StringUtils.create(s)));
    }

    public void testItWorksWithNullAndEmpty() {
        assertEquals("null", "", StringUtils.create(StringUtils.create((String)null)));
        assertEquals("empty", "", StringUtils.create(StringUtils.create("")));
    }
}
