package uk.ac.warwick.util.content.texttransformers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes CSS as input and rewrites url() references to be
 * absolute, using the given pageurlpath as base.
 */
public final class CssUrlRewriteTransformer extends TextPatternTransformer {
	
	private URL base;
	
	private Pattern pattern;
	
	public CssUrlRewriteTransformer(String theBase) throws MalformedURLException {
		base = new URL(theBase);
	}
	
	public String transform(String input) {
	    return transform(input, new Handler());
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
        public String transform(String input) {
            Matcher matcher = getPattern().matcher(input);
            if (matcher.find()) {
            	String url = matcher.group(2);
            	return "url("+parseUrl(url)+")";
            }
            return input;
        }
	}

	private String parseUrl(final String url) {
        // take the URL and, if necessary, absolute it
        try {
            return (new URL(base, url).toExternalForm());
        } catch (MalformedURLException e) {
            return url;
        }
    }
}
