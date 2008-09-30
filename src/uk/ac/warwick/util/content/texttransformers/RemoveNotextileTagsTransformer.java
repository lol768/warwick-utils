package uk.ac.warwick.util.content.texttransformers;


public class RemoveNotextileTagsTransformer implements TextTransformer {

	static final String NOTEXTILE_TAG_PATTERN = "<\\/?notextile>";

	public String transform(final String text) {
		String html = text;

		return html.replaceAll(NOTEXTILE_TAG_PATTERN, "");
	}

}
