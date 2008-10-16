package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VimeoMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?vimeo.com/(?:clip:)?(\\d+)", Pattern.CASE_INSENSITIVE);
    
    private static final ThreadLocal<String> VIDEO_ID = new ThreadLocal<String>();
    
    public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url.toString());
    	boolean gotVideoId = matcher.matches();
    	if (gotVideoId) {
    		VIDEO_ID.set( matcher.group(1) );
    	}
        return gotVideoId;
    }

    @Override
    public String getFlashUrl(final String url) {
        return "http://vimeo.com/moogaloop.swf?clip_id=" + VIDEO_ID.get();
    }
}