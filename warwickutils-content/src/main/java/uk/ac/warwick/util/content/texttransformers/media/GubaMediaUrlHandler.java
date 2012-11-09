package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GubaMediaUrlHandler extends
		AbstractFlashPlayerMediaUrlHandler {

	private static final Pattern PATTERN = Pattern.compile(
			"http://(?:www\\.)?guba\\.com/watch/(\\d+).*",
			Pattern.CASE_INSENSITIVE);

    private static final ThreadLocal<String> VIDEO_ID = new ThreadLocal<String>();

	public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url.toString());
    	boolean itMatches = matcher.matches();
    	if (itMatches) {
    		VIDEO_ID.set(matcher.group(1));
    	}
        return itMatches;
    }

	@Override
	public String getFlashUrl(final String url) {
		return "http://www.guba.com/f/root.swf?video_url=http://free.guba.com/uploaditem/"
				+ VIDEO_ID.get() + "/flash.flv&isEmbeddedPlayer=true";
	}
}