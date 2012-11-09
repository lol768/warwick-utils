package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public class HtmlUtilsTest extends TestCase {
	/**
	 * Test the function that escapes only non-ASCII characters -
	 * it leaves HTML brackets and ampersands as is, so its purpose
	 * is for storing what you want to be displayed as HTML, in
	 * a container that doesn't support Unicode.
	 * 
	 * A prime example is preparing page content for Sitebuilder -
	 * browsers will do this for you, but when not submitting from
	 * a browser this is necessary to ensure that Sitebuilder
	 * can store it without problems.
	 */
	public void testEscapeHtmlForAscii() {
		// Input contains Cyrillic that we _do_ want converted, as well as
		// HTML brackets and existing HTML entities that we _don't_ want
		// converted.
		String input = "<html>Yes? \u0414\u0430! (&copy; 2008)</html>"; //Cyrillic "Da"
		String expected = "<html>Yes? &#x414;&#x430;! (&copy; 2008)</html>";
		String result = HtmlUtils.htmlEscapeNonAscii(input);
		assertEquals(expected, result);
	}
}
