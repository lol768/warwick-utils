package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class MySpaceMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://vids\\.myspace\\.com/index.cfm\\?(.*videoid=\\d+.*)", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toURL(url).getQuery();
        String videoId = extractVideoId(params, "videoid");
        return "http://lads.myspace.com/videos/vplayer.swf?m=" + videoId + "&type=video";
    }
}