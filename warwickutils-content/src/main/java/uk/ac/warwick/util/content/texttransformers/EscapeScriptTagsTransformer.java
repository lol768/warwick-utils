package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

public final class EscapeScriptTagsTransformer implements TextTransformer {

    static final Pattern SCRIPT_TAGS = Pattern.compile("<script.*>.*</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public MutableContent apply(MutableContent mc) {
        String html = mc.getContent();

		// Quick escape
		if (!(html.toLowerCase().indexOf("<script") > -1)) {
			return mc;
		}

        mc = new HtmlCommentEscapingTransformer().apply(mc);

        return mc;
	}

    private static class HtmlCommentEscapingTransformer extends TextPatternTransformer {
        protected Pattern getPattern() {
            return SCRIPT_TAGS;
        }

        @Override
        protected boolean isGeneratesHead() {
            return false;
        }

        @Override
        protected Callback getCallback() {
            return new TextPatternTransformer.Callback() {
                public String transform(final String input, final MutableContent mc) {
                    Matcher matcher = SCRIPT_TAGS.matcher(input);
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