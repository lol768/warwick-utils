package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class GoogleMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern GOOGLE_VIDEO_PATTERN = Pattern.compile(Pattern.quote("http://video.google.") + "(?:com|co\\.uk)/videoplay\\?(.*docId=.+)", Pattern.CASE_INSENSITIVE);

    public boolean recognises(final String url) {
        return GOOGLE_VIDEO_PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toUri(url).getQuery();
        String videoId = extractVideoId(params, "docid");
        return "http://video.google.com/googleplayer.swf?docId=" + videoId;
    }
}