package uk.ac.warwick.util.content.texttransformers;


public class RemoveEmptyParagraphsTransformer implements TextTransformer {

	static final String EMPTY_PARAGRAPH_PATTERN = "<p>(\\&nbsp\\;)?<\\/p>";

	public String transform(final String text) {
		return text.replaceAll(EMPTY_PARAGRAPH_PATTERN, "");
	}

}
