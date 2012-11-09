package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;


public class RemoveNotextileTagsTransformer implements TextTransformer {

	static final String NOTEXTILE_TAG_PATTERN = "<\\/?notextile>";

	public MutableContent apply(MutableContent mc) {
		String html = mc.getContent();

		mc.setContent(html.replaceAll(NOTEXTILE_TAG_PATTERN, ""));
		return mc;
	}

}
