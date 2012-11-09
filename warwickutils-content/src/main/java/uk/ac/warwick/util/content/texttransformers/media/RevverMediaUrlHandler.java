package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RevverMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:.+\\.)?revver\\.com/watch/(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final ThreadLocal<String> VIDEO_ID = new ThreadLocal<String>();
    
    public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url.toString());
    	boolean gotIt = matcher.matches();
    	if (gotIt) {
    		VIDEO_ID.set(matcher.group(1));
    	}
        return gotIt;
    }

    @Override
    public String getFlashUrl(final String url) {
        return "http://flash.revver.com/player/1.0/player.swf?mediaId=" + VIDEO_ID.get() + "&affiliateId=0";
    }
}