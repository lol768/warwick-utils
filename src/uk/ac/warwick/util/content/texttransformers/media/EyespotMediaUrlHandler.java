package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class EyespotMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?eyespot\\.com/detail\\?(.*playArg=.+)", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toURL(url).getQuery();
        String videoId = extractVideoId(params, "playArg");
        return "http://eyespot.com/flash/flvplayer.swf?contextId=11&vurl=http%3A%2F%2Fdownloads.eyespot.com%2Fplay%3Fr%3D" + videoId;
    }
}