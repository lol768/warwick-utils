package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class SelfcastTVMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?selfcasttv\\.com/Selfcast/playVideo.do\\?(.*ref=.+)", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toURL(url).getQuery();
        String videoId = extractVideoId(params, "ref");
        return "http://www.selfcasttv.com/Selfcast/selfcast.swf?video_1=/" + videoId;
    }
}