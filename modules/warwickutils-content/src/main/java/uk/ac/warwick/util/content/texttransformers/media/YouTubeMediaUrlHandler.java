package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class YouTubeMediaUrlHandler extends AbstractEmbeddedFrameMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("https?://(?:.*\\.)?youtube\\.com/watch\\?.*v=[^&]+.*", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getEmbedUrl(final String url) {
        String params = toUri(url).getQuery();
        String videoId = extractVideoId(params, "v");
        return "https://www.youtube.com/embed/" + videoId + "?wmode=transparent&rel=0";
    }
}