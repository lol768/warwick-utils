package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.web.Uri;

/**
 * Takes CSS as input and rewrites url() references to be
 * absolute, using the given pageurlpath as base.
 */
public final class CssUrlRewriteTransformer extends TextPatternTransformer {
	
	private final Uri base;
	
	private Pattern pattern;
	
	public CssUrlRewriteTransformer(String theBase) {
		this(Uri.parse(theBase));
	}
	
	public CssUrlRewriteTransformer(Uri theBase) {
	    this.base = theBase;
	}
	
	@Override
    protected Callback getCallback() {
        return new Handler();
    }

    @Override
    protected boolean isGeneratesHead() {
        return false;
    }

    @Override
	protected Pattern getPattern() {
		/* matches url(xx), url('xx'), url("xx") */
		if (pattern == null) {
		    String quote = "([\"']?)";
		    String path = "([^\"')]+)";
		    String unquote = "\\1";
		    pattern = Pattern.compile("url\\("+quote+path+unquote+"\\)", Pattern.CASE_INSENSITIVE);
		}
		return pattern;
	}
	
	public class Handler implements Callback {
        public String transform(String input, MutableContent mc) {
            Matcher matcher = getPattern().matcher(input);
            if (matcher.find()) {
            	String url = matcher.group(2);
            	return "url("+parseUrl(url).toString()+")";
            }
            return input;
        }
	}

	private Uri parseUrl(final String url) {
        // take the URL and, if necessary, absolute it
	    return base.resolve(Uri.parse(url));
    }
}
