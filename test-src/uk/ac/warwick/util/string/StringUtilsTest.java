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
}
