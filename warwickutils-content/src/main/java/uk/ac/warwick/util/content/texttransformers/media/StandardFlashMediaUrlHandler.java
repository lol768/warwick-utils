package uk.ac.warwick.util.content.texttransformers.media;

import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.Uri.UriException;

/**
 * Very simple flash-based handler which just shows the SWF file supplied.
 */
public final class StandardFlashMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
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
    
    @Override
    public String getFlashUrl(final String url) {
        return url;
    }
}
