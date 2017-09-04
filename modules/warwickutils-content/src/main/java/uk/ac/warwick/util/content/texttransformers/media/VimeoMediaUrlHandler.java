package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VimeoMediaUrlHandler extends AbstractEmbeddedFrameMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("https://(?:www\\.)?vimeo.com/(?:clip:)?(\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean recognises(final String url) {
    	Matcher matcher = PATTERN.matcher(url);
    	return matcher.matches();
    }

    @Override
    public String getEmbedUrl(final String url) {
        Matcher matcher = PATTERN.matcher(url);
        if (!matcher.matches()) throw new IllegalStateException();
        return "https://player.vimeo.com/video/" + matcher.group(1);
    }
}
