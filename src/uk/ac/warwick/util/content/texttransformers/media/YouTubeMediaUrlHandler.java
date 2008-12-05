package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class YouTubeMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:.*\\.)?youtube\\.com/watch\\?.*v=[^&]+.*", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toURL(url).getQuery();
        String videoId = extractVideoId(params, "v");
        return "http://www.youtube.com/v/" + videoId + "&fs=1&hl=en";
    }
}