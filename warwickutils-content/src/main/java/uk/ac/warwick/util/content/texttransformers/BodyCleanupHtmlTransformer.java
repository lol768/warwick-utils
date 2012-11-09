package uk.ac.warwick.util.content.texttransformers;

import java.util.List;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.cleaner.Cleaner;
import uk.ac.warwick.util.core.HtmlUtils;

public final class BodyCleanupHtmlTransformer implements TextTransformer {
    
    private final Cleaner cleaner;
    
    public BodyCleanupHtmlTransformer(final Cleaner theCleaner) {
        this.cleaner = theCleaner;
    }
    
    public MutableContent apply(MutableContent mc) {
        String text = mc.getContent();
        String result;
        List<String> bodies = HtmlUtils.extractContent(text, "<body", "</body>");
        
        if ((bodies).size() > 0) {
            String body = bodies.iterator().next();
            body = cleaner.clean(body, mc);
            result = HtmlUtils.replaceContent(text, "\n\n" + body + "\n", "<body", "</body>");
        } else {
            result = cleaner.clean(text, mc);
        }
        
        mc.setContent(result);
        return mc;
    }
}
