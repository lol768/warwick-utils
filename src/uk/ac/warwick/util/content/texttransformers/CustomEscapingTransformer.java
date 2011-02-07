package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

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

	public MutableContent apply(MutableContent mc) {
		String html = mc.getContent();

		// Quick escape
		if (!((html.indexOf("\n\\\\") > -1) || (html.indexOf("\\A\\\\") > -1))) {
			return mc;
		}

		mc = new FourSlashEscapingTransformer().apply(mc);
		mc = new ThreeSlashEscapingTransformer().apply(mc);
		mc = new TwoSlashEscapingTransformer().apply(mc);

		return mc;
	}

	private static class FourSlashEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return EXP_TEXTILE_BLOCK_ESC_NO_LINE_BREAKS_PAIR_MATCH;
		}

        @Override
        protected Callback getCallback() {
           return new TextPatternTransformer.Callback() {
               public String transform(final String input, final MutableContent mc) {
                   Matcher matcher = EXP_TEXTILE_BLOCK_ESC_NO_LINE_BREAKS_PAIR_MATCH.matcher(input);
                   if (!matcher.matches()) {
                       throw new IllegalStateException(
                               "Failed to match four escapes, but shouldn't be here if it didn't");
                   }

                   String inner = matcher.group(1);

                   return "<notextile><p>" + inner + "</p></notextile>\n\n";
               }
           };
        }
	}

	private static class ThreeSlashEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return EXP_TEXTILE_BLOCK_ESC_AND_CODE_PAIR_MATCH;
		}

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {
                public String transform(final String input, final MutableContent mc) {
                    Matcher matcher = EXP_TEXTILE_BLOCK_ESC_AND_CODE_PAIR_MATCH
                            .matcher(input);
                    if (!matcher.matches()) {
                        throw new IllegalStateException(
                                "Failed to match three escapes, but shouldn't be here if it didn't");
                    }

                    return "<pre><code>" + matcher.group(1) + "</code></pre>\n\n";
                }
            };
        }
	}

	private static class TwoSlashEscapingTransformer extends TextPatternTransformer {
		protected Pattern getPattern() {
			return EXP_TEXTILE_BLOCK_ESC_PAIR_MATCH;
		}

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {
                public String transform(final String input, final MutableContent mc) {
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
            };
        }
	}
}