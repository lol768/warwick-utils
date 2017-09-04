package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MetacafeMediaUrlHandler extends AbstractEmbeddedFrameMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("https?://(?:www\\.)?metacafe\\.com/watch/(\\d+)/([^/]+)/?", Pattern.CASE_INSENSITIVE);

    public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url);
    	return matcher.matches();
    }

    @Override
    public String getEmbedUrl(String url) {
        Matcher matcher = PATTERN.matcher(url);
        if (!matcher.matches()) throw new IllegalStateException();

        return "https://www.metacafe.com/embed/" + matcher.group(1) + "/" + matcher.group(2) + "/";
    }

}