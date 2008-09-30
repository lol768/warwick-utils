package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;

import uk.ac.warwick.util.content.freemarker.FreeMarkerRenderingUtils;

public abstract class MediaUrlHandler {
    public abstract boolean recognises(String url);
    public abstract String getHtml(String url, Map<String,Object> parameters);
    
    protected final String renderTemplate(final String templateName, final Map<String,Object> model) {
        return "<notextile>" + FreeMarkerRenderingUtils.processTemplate(templateName, model).toString() + "</notextile>";
    }
}
