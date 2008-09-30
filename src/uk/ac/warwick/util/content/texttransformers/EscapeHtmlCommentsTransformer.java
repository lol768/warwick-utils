package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EscapeHtmlCommentsTransformer implements TextTransformer {

	static final Pattern HTML_COMMENT = Pattern
			.compile(
					"<\\!(\\-)+.*(\\-)+>",
					Pattern.DOTALL);

	public String transform(final String text) {
		String html = text;

		// Quick escape
		if (!(html.indexOf("<!") > -1)) {
			return html;
		}

		html = new HtmlCommentEscapingTransformer().transform(html,
				new TextPatternTransformer.Callback() {
					@SuppressWarnings("unchecked")
					public String transform(final String input) {
						Matcher matcher = HTML_COMMENT
								.matcher(input);
						if (!matcher.matches()) {
							throw new IllegalStateException(
									"Failed to match any HTML comments, but shouldn't be here if it didn't");
						}

						String inner = matcher.group(0);

						return "<notextile>" + inner + "</notextile>";
					}

				});

		return html;
	}

	class HtmlCommentEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return HTML_COMMENT;
		}
	}

}