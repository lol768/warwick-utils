package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IFilmMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?ifilm\\.com/video/(\\d+)", Pattern.CASE_INSENSITIVE);
    
    private static final ThreadLocal<String> VIDEO_ID = new ThreadLocal<String>();
    
    public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url.toString());
    	boolean matches = matcher.matches();
    	if (matches) {
    		VIDEO_ID.set(matcher.group(1));
    	}
        return matches;
    }

    @Override
    public String getFlashUrl(final String url) {
        return "http://www.ifilm.com/efp?flvbaseclip=" + VIDEO_ID.get();
    }
}