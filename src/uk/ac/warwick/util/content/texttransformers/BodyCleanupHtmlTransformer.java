package uk.ac.warwick.util.content.texttransformers;

import java.util.List;

import uk.ac.warwick.util.content.cleaner.HtmlCleaner;
import uk.ac.warwick.util.core.HtmlUtils;

public final class BodyCleanupHtmlTransformer implements TextTransformer {
    
    private final HtmlCleaner cleaner;
    
    public BodyCleanupHtmlTransformer(final HtmlCleaner theCleaner) {
        this.cleaner = theCleaner;
    }
    
    public String transform(final String text) {
        String result;
        List<String> bodies = HtmlUtils.extractContent(text, "<body", "</body>");
        
        if ((bodies).size() > 0) {
            String body = bodies.iterator().next();
            body = cleaner.clean(body);
            result = HtmlUtils.replaceContent(text, "\n\n" + body + "\n", "<body", "</body>");
        } else {
            result = cleaner.clean(text);
        }
        
        return result;
    }
}
