package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

public final class EscapeHtmlCommentsTransformer implements TextTransformer {

    static final Pattern HTML_COMMENT = Pattern.compile("<\\!(\\-)+.*(\\-)+>", Pattern.DOTALL);

    public MutableContent apply(MutableContent mc) {
		String html = mc.getContent();

		// Quick escape
		if (!(html.indexOf("<!") > -1)) {
			return mc;
		}

		mc = new HtmlCommentEscapingTransformer().apply(mc);

		return mc;
	}

    private static class HtmlCommentEscapingTransformer extends TextPatternTransformer {
        @Override
        protected Pattern getPattern() {
            return HTML_COMMENT;
        }

        @Override
        protected boolean isGeneratesHead() {
            return false;
        }

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {
                public String transform(final String input, final MutableContent mc) {
                    Matcher matcher = HTML_COMMENT.matcher(input);
                    if (!matcher.matches()) {
                        throw new IllegalStateException("Failed to match any HTML comments, but shouldn't be here if it didn't");
                    }

                    String inner = matcher.group(0);

                    return "<notextile>" + inner + "</notextile>";
                }
            };
        }
    }

}