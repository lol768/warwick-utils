package uk.ac.warwick.util.content.texttransformers;


public class RemoveLeadingNbspTransformer implements TextTransformer {

	static final String LEADING_NBSP_PATTERN = "<([^>]+)>\\&nbsp\\;";

	public String transform(final String text) {
		return text.replaceAll(LEADING_NBSP_PATTERN, "<$1>");
	}

}
