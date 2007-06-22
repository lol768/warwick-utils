package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public final class StringUtilsConvertToArrayTest extends TestCase {
	public void testSingleStringToArray() {
		String s = "a single string";
		String[] results = StringUtils.convertToArray(s);
		assertEquals("no of elements", 1, results.length);
		assertEquals("s", s, results[0]);
	}
	
	public void testArray() {
		String firstString = "firstString";
		String secondString = "secondString";
		String s = firstString + "," + secondString;
		String[] results = StringUtils.convertToArray(s);
		assertEquals("no of elements", 2, results.length);
		assertEquals("first string", firstString, results[0]);
		assertEquals("second string", secondString, results[1]);
	}
}
