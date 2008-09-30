package uk.ac.warwick.util.content.texttransformers.media;

import uk.ac.warwick.util.core.FileUtils;

public abstract class AbstractExtensionMatchingMediaUrlHandler extends MediaUrlHandler {
    
    protected abstract String[] getSupportedExtensions();
    
    public final boolean recognises(final String url) {
        String urlString = url.toString();
        String urlExtension = FileUtils.getLowerCaseExtension(urlString);
        for (String extension : getSupportedExtensions()) {
            if (extension.equals(urlExtension)) {
                return true;
            }
        }
        return false;
    }
}
