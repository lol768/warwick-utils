package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CustomEscapingTransformer implements TextTransformer {

	static final Pattern EXP_TEXTILE_BLOCK_ESC_PAIR_MATCH = Pattern
			.compile(
					"(?:(?:\\n\\s*?|\\A\\s*?|^\\s*?)+?)(?:\\\\\\\\)(?:\\s*\\n+?)(\\W|.+?)(?:\\n+?)(?:\\\\\\\\??)(?:\\s*?\\n|\\s*?\\z)",
					Pattern.DOTALL);

	static final Pattern EXP_TEXTILE_BLOCK_ESC_AND_CODE_PAIR_MATCH = Pattern
			.compile(
					"(?:(?:\\n\\s*?|\\A\\s*?|^\\s*?)+?)(?:\\\\\\\\\\\\)(?:\\s*\\n+?)(\\W|.+?)(?:\\n+?)(?:\\\\\\\\\\\\??)(?:\\s*?\\n|\\s*?\\z)",
					Pattern.DOTALL);

	static final Pattern EXP_TEXTILE_BLOCK_ESC_NO_LINE_BREAKS_PAIR_MATCH = Pattern
			.compile(
					"(?:(?:\\n\\s*?|\\A\\s*?|^\\s*?)+?)(?:\\\\\\\\\\\\\\\\)(?:\\s*\\n+?)(\\W|.+?)(?:\\n+?)(?:\\\\\\\\\\\\\\\\??)(?:\\s*?\\n|\\s*?\\z)",
					Pattern.DOTALL);

	static final String EXP_NEW_LINE = "(.*)\\n(.*)";

	static final String REPLACE_LINEBREAK = "$1<br />$2";

	public String transform(final String text) {
		String html = text;

		// Quick escape
		if (!((html.indexOf("\n\\\\") > -1) || (html.indexOf("\\A\\\\") > -1))) {
			return html;
		}

		html = new FourSlashEscapingTransformer().transform(html,
				new TextPatternTransformer.Callback() {
					public String transform(final String input) {
						Matcher matcher = EXP_TEXTILE_BLOCK_ESC_NO_LINE_BREAKS_PAIR_MATCH
								.matcher(input);
						if (!matcher.matches()) {
							throw new IllegalStateException(
									"Failed to match four escapes, but shouldn't be here if it didn't");
						}

						String inner = matcher.group(1);

						return "<notextile><p>" + inner + "</p></notextile>\n\n";
					}

				});

		html = new ThreeSlashEscapingTransformer().transform(html,
				new TextPatternTransformer.Callback() {
					public String transform(final String input) {
						Matcher matcher = EXP_TEXTILE_BLOCK_ESC_AND_CODE_PAIR_MATCH
								.matcher(input);
						if (!matcher.matches()) {
							throw new IllegalStateException(
									"Failed to match three escapes, but shouldn't be here if it didn't");
						}

						return "<pre><code>" + matcher.group(1) + "</code></pre>\n\n";
					}

				});

		html = new TwoSlashEscapingTransformer().transform(html,
				new TextPatternTransformer.Callback() {
					public String transform(final String input) {
						Matcher matcher = EXP_TEXTILE_BLOCK_ESC_PAIR_MATCH
								.matcher(input);
						if (!matcher.matches()) {
							throw new IllegalStateException(
									"Failed to match two escapes, but shouldn't be here if it didn't");
						}

						// keep line breaks
						String inner = matcher.group(1).replaceAll(
								EXP_NEW_LINE, REPLACE_LINEBREAK);

						return "<notextile><p>" + inner + "</p></notextile>\n\n";
					}

				});

		return html;
	}

	class FourSlashEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return EXP_TEXTILE_BLOCK_ESC_NO_LINE_BREAKS_PAIR_MATCH;
		}
	}

	class ThreeSlashEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return EXP_TEXTILE_BLOCK_ESC_AND_CODE_PAIR_MATCH;
		}
	}

	class TwoSlashEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return EXP_TEXTILE_BLOCK_ESC_PAIR_MATCH;
		}
	}
}