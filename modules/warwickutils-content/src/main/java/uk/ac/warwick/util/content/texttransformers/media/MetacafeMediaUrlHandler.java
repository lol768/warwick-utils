package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MetacafeMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?metacafe\\.com/watch/(\\d+)/([^/]+)/?", Pattern.CASE_INSENSITIVE);
    
    private static final ThreadLocal<String> VIDEO_ID = new ThreadLocal<String>();
    private static final ThreadLocal<String> TITLE = new ThreadLocal<String>();
    
    public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url.toString());
    	boolean gotMatch = matcher.matches();
    	if (gotMatch) {
    		VIDEO_ID.set( matcher.group(1) );
    		TITLE.set( matcher.group(2) );
    	}
        return gotMatch;
    }

    @Override
    public String getFlashUrl(final String url) {
        return "http://www.metacafe.com/fplayer/" + VIDEO_ID.get() + "/" + TITLE.get() + ".swf";
    }
}