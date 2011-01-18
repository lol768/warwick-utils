package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;

import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;
import uk.ac.warwick.util.core.HtmlUtils;
import uk.ac.warwick.util.core.HttpUtils;

public final class LatexTextTransformer extends AbstractSquareTagTransformer {
    
    private final String latexUrl;
    
    public LatexTextTransformer(final String theLatexUrl) {
        super("latex", true); //true = multiline
        this.latexUrl = theLatexUrl;
    }

    @Override
    protected String[] getAllowedParameters() {
        return new String[0];
    }

    @Override
    protected Callback getCallback() {
        return new TextPatternTransformer.Callback() {
            public String transform(final String input) {
                Matcher matcher = getTagPattern().matcher(input);
                if (!matcher.matches()) {
                    throw new IllegalStateException(
                            "Failed to match latex tag, but shouldn't be here if it didn't");
                }

                String contents = getContents(matcher);
                
                if (contents == null) {
                    return input;
                }
                
                String urlEncodedLatex = HttpUtils.utf8Encode(contents.replaceAll(" ", "~"));
				String htmlEncodedLatex = HtmlUtils.htmlEscape(contents);
				
				return "<notextile><img class=\"latex\" src=\"" + latexUrl
						+ "?" + urlEncodedLatex + "\" alt=\"" + htmlEncodedLatex + "\"></notextile>";
            }
        };
    }

}
