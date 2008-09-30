package uk.ac.warwick.util.content.texttransformers.media;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Very simple flash-based handler which just shows the SWF file supplied.
 */
public final class StandardFlashMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    @Override
    public boolean recognises(final String url) {
        String path;
        try {
            URL realUrl = new URL(url);
            path = realUrl.getPath();
        } catch (MalformedURLException e) {
            path = url;
        }
        return path.toLowerCase().endsWith(".swf");
    }
    
    @Override
    public String getFlashUrl(final String url) {
        return url;
    }
}
