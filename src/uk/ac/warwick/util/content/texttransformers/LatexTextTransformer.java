package uk.ac.warwick.util.content.texttransformers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;

import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;
import uk.ac.warwick.util.core.HtmlUtils;

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
                
                String urlEncodedLatex;
				String htmlEncodedLatex = HtmlUtils.htmlEscape(contents);
				try {
					urlEncodedLatex = URLEncoder.encode(contents.replaceAll(" ", "~"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(
							"Latex URL encoding failed");
				}
				
				return "<notextile><img class=\"latex\" src=\"" + latexUrl
						+ "?" + urlEncodedLatex + "\" alt=\"" + htmlEncodedLatex + "\"></notextile>";
            }
        };
    }

}
