package uk.ac.warwick.util.content.texttransformers.media;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.Uri.UriException;

import java.util.HashMap;
import java.util.Map;

/**
 * Very simple flash-based handler which just shows the SWF file supplied.
 */
public final class StandardFlashMediaUrlHandler extends MediaUrlHandler {

    @Override
    public String getHtml(final String url, final Map<String,Object> parameters, MutableContent mc) {
        String flashUrl = getFlashUrl(url);
        if (flashUrl != null) {
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("url", flashUrl);

            model.putAll(parameters);
            return renderTemplate("media/flashvideo.ftl", model);
        } else {
            throw new IllegalStateException("This Media URL handler doesn't recognise the given URL");
        }
    }

    @Override
    public boolean recognises(final String url) {
        String path;
        try {
            path = Uri.parse(url).getPath();
        } catch (UriException e) {
            path = url;
        }
        return path.toLowerCase().endsWith(".swf");
    }

    private String getFlashUrl(final String url) {
        return url;
    }
}
