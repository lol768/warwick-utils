package uk.ac.warwick.util.content.texttransformers;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.MutableContent;

public final class NoFollowLinkTransformerTest extends TestCase {

	public void testBasicRewrite() {
		String input = "Text text <a href=\"something\">Link</a>";
		String expected = "Text text <a rel=\"nofollow\" href=\"something\">Link</a>";

		verify(input, expected);
	}

	public void testTwoLinks() {
		String input = "Text text <a href=\"something\">Link</a> and <a href=\"somethingelse\">Another Link</a>";
		String expected = "Text text <a rel=\"nofollow\" href=\"something\">Link</a> and <a rel=\"nofollow\" href=\"somethingelse\">Another Link</a>";

		verify(input, expected);
	}

	public void testOtherAttributes() {
		String input = "Text text <a href=\"something\" target=\"_blank\">Link</a>";
		String expected = "Text text <a rel=\"nofollow\" href=\"something\" target=\"_blank\">Link</a>";

		verify(input, expected);
	}

	public void testAlreadyNoFollow() {
		String input = "Text text <a href=\"something\" rel=\"nofollow\">Link</a>";
		String expected = "Text text <a rel=\"nofollow\" href=\"something\">Link</a>";

		verify(input, expected);
	}

	public void testAnotherRel() {
		String input = "Text text <a href=\"something\" rel=\"prefetch\">Link</a>";
		String expected = "Text text <a rel=\"nofollow prefetch\" href=\"something\">Link</a>";

		verify(input, expected);
	}

	/**
	 * BB-1307
	 */
	public void testDollarInString() {
		String input = "Text text <a href=\"something\">Links cost $</a>";
		String expected = "Text text <a rel=\"nofollow\" href=\"something\">Links cost $</a>";

		verify(input, expected);
	}

	public void testVaryCaseTags() {
		String input = "Text text <A Href=\"something\">Link</a>";
		String expected = "Text text <A rel=\"nofollow\" Href=\"something\">Link</a>";

		verify(input, expected);
	}

	public void testEmptyLink() {
		String input = "Text test <a>Blah</a>";
		verify(input, input);
	}

	private void verify(final String input, final String expected) {
		NoFollowLinkTransformer parser = new NoFollowLinkTransformer();
		assertEquals(expected, parser.apply(new MutableContent(null, input)).getContent());
	}

}
