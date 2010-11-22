package uk.ac.warwick.util.string;

import static uk.ac.warwick.userlookup.cache.Pair.*;
import static uk.ac.warwick.util.core.StringUtils.*;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import uk.ac.warwick.userlookup.cache.Pair;
import uk.ac.warwick.util.core.StringUtils;

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

	@SuppressWarnings("unchecked")
    public void testHtmlEscapeSpecialCharacters() {
	    for (Pair<String,String> pair: Arrays.asList(
	            of("Regular string", "Regular string"),
	            of("Some <b>bold</b> text", "Some &lt;b&gt;bold&lt;/b&gt; text"),
	            of("Ben & Jerry's", "Ben &amp; Jerry's"),
	            of("I have to go to A&E", "I have to go to A&amp;E"),
	            of("a - \u1234", "a - \u1234"),    // doesn't deal with non-ASCII characters
	            of("&Agrave; < bient&ocirc;t", "&Agrave; &lt; bient&ocirc;t"), // doesn't touch existing HTML entities
	            of("I think & &there4; I am", "I think &amp; &there4; I am"),
	            of("Dec entity: &#1234;!", "Dec entity: &#1234;!"),
	            of("Hex entity: &#x123;!", "Hex entity: &#x123;!"),
	            of("<esc/> 3 &times; 4 = 12, A&E", "&lt;esc/&gt; 3 &times; 4 = 12, A&amp;E") // example in JIRA
	    )) {
	        assertEquals(pair.getSecond(), htmlEscapeSpecialCharacters(pair.getFirst()));
	    }
	}
	
}
