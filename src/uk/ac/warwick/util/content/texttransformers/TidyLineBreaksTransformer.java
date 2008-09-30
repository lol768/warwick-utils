package uk.ac.warwick.util.content.texttransformers;

public class TidyLineBreaksTransformer implements TextTransformer {

	public String transform(final String text) {
		String html = text;

		// Windows line breaks
		html = html.replaceAll("\r\n", "\n");
		html = html.replaceAll("\r", "\n");

		// Replace tabs with space equivalent
		html = html.replaceAll("\t", "    ");

		// empty entry
		html = html.replaceAll("^ +$", "");

		// three or more consecutive newlines
		html = html.replaceAll("\n{3,}", "\n\n");

		// punctuation at the end
		html = html.replaceAll("\"$", "\" ");

		return html;
	}

}
