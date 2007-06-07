package uk.ac.warwick.util.string;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {
	public void testJoinStringArray() {
		String[] input = new String[] {"one","two","three","four"};
		String expected = "one|two|three|four";
		assertEquals(expected, StringUtils.join(input, "|"));
	}
	
	public void testJoinStringList() {
		List<String> input = Arrays.asList(new String[] {"one","two","three","four"});
		String expected = "one&two&three&four";
		assertEquals(expected, StringUtils.join(input, "&"));
	}
	
	public void testLtrim() {
		String input = "   trim me   ";
		String expected = "trim me   ";
		assertEquals(expected, StringUtils.ltrim(input));
	}
	
	public void testRtrim() {
		String input = "   trim me   ";
		String expected = "   trim me";
		assertEquals(expected, StringUtils.rtrim(input));
	}
	
	public void testHasLength() {
		assertTrue(StringUtils.hasLength("abc"));
		assertTrue(StringUtils.hasLength(" "));
		
		assertFalse(StringUtils.hasLength(""));
		assertFalse(StringUtils.hasLength(null));
	}
	
	public void testHasText() {
		assertTrue(StringUtils.hasText("abc"));
		
		assertFalse(StringUtils.hasText("  "));
		assertFalse(StringUtils.hasText(" \n "));
		assertFalse(StringUtils.hasText(" \n\t \r\n "));
		assertFalse(StringUtils.hasText(""));
		assertFalse(StringUtils.hasText(null));
	}
	
	public void testCompactWhitespace() {
		String input = "Large \n   amounts  \r\n\r\n  of \n\n\t   whitespace    to   \n compact";
		String expected = "Large amounts of whitespace to compact";
		
		assertEquals(expected, StringUtils.compactWhitespace(input));
	}
	
	public void testSafeSubstring() {
		String input = "String of a certain length";
		
		assertEquals("of a", StringUtils.safeSubstring(input, 7, 11));
		assertEquals("leng", StringUtils.safeSubstring(input, 20, 24));
		assertEquals("length", StringUtils.safeSubstring(input, 20, 28));
		assertEquals("", StringUtils.safeSubstring(input, 30, 40));
		assertEquals("", StringUtils.safeSubstring(null, 1000, 3213));
	}
}
