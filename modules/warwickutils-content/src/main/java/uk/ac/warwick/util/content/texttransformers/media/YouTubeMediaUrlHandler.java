package uk.ac.warwick.util.content.texttransformers.media;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

public final class YouTubeMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:.*\\.)?youtube\\.com/watch\\?.*v=[^&]+.*", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toUri(url).getQuery();
        String videoId = extractVideoId(params, "v");
        return "http://www.youtube.com/embed/" + videoId + "?wmode=transparent";
    }
    
    @Override
    public String getHtml(final String url, final Map<String,Object> parameters, MutableContent mc) {
        String flashUrl = getFlashUrl(url);
        if (flashUrl != null) {
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("url", flashUrl);
            
            model.putAll(parameters);
            return renderTemplate("media/youtube.ftl", model);
        } else {
            throw new IllegalStateException("This Media URL handler doesn't recognise the given URL");
        }
    }
}