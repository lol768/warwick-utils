package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class JumpcutMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?jumpcut\\.com/view\\?(.*id=.+)", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toURL(url).getQuery();
        String videoId = extractVideoId(params, "id");
        return "http://www.jumpcut.com/media/flash/jump.swf?id=" + videoId + "&asset_type=movie&asset_id=" + videoId + "&eb=1";
    }
}