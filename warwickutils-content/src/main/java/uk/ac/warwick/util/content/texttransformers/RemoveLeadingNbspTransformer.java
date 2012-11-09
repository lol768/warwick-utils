package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;


public class RemoveLeadingNbspTransformer implements TextTransformer {

	static final String LEADING_NBSP_PATTERN = "<([^>]+)>\\&nbsp\\;";

	public MutableContent apply(MutableContent mc) {
		String text = mc.getContent();
		text = text.replaceAll(LEADING_NBSP_PATTERN, "<$1>");
		mc.setContent(text);
		return mc;
	}

}
