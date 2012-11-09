package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;


public class RemoveEmptyParagraphsTransformer implements TextTransformer {

	static final String EMPTY_PARAGRAPH_PATTERN = "<p>(\\&nbsp\\;)?<\\/p>";

	public MutableContent apply(MutableContent mc) {
		String text = mc.getContent();
		text = text.replaceAll(EMPTY_PARAGRAPH_PATTERN, "");
		mc.setContent(text);
		return mc;
	}

}
