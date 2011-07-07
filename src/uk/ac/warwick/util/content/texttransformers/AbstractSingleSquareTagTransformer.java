package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.warwick.util.content.MutableContent;

/**
 * Really quite similar to AbstractSquareTagTransformer, but only looks for
 * single tags, and so naturally doesn't check for tag contents.
 */
public abstract class AbstractSingleSquareTagTransformer implements TextTransformer {
    
    public static final int PARAMETERS_MATCH_GROUP = 1;
    private static final Logger LOGGER = Logger.getLogger(AbstractSingleSquareTagTransformer.class);
    
    private final Pattern tagPattern; 
    private final String tagName;
    private boolean doQuickCheck = true;
    
    public AbstractSingleSquareTagTransformer(final String theTagName) {
        tagPattern = getPatternForTagName(theTagName);
        tagName = theTagName;
    }
 
    protected abstract TextPatternTransformer.Callback getTagCallback();
    
    protected abstract String[] getAllowedParameters();
    
    public final MutableContent apply(MutableContent mc) {
        String html = mc.getContent();
        //Quick escape
        if (doQuickCheck && html.toLowerCase().indexOf(("[" + tagName).toLowerCase()) == -1) {
            return mc;
        }
        try {
            mc = new TagTransformer().apply(mc);
        } catch (Exception e) {
            LOGGER.error("Caught exception trying to run square bracket", e);
        }
        return mc;
    }
    
    /**
     * Deliberately not final, allow to be overridden
     */
    public boolean applies(MutableContent mc) {
        String html = mc.getContent();
        
        if (html.toLowerCase().indexOf(("[" + tagName).toLowerCase()) == -1) {
            return false;
        }
        
        return getTagPattern().matcher(html).find();
    }
    
    public final Pattern getTagPattern() {
        return tagPattern;
    }
    
    public abstract boolean isTagGeneratesHead();
    
    /**
     * The class which goes through the input, matching patterns
     * and using the callback to return the transformed text.
     */
    private class TagTransformer extends TextPatternTransformer {
        @Override
        protected Pattern getPattern() {
            return getTagPattern();
        }
        @Override
        protected Callback getCallback() {
            return getTagCallback();
        }
        @Override
        protected boolean isGeneratesHead() {
            return isTagGeneratesHead();
        }
    }
    
    private Pattern getPatternForTagName(final String theTagName) {
        int flags = Pattern.CASE_INSENSITIVE;       
        String pattern = "\\[" + theTagName +                //opening tag
        "(\\s+.+?[^\\\\])?" +          //optional parameters (can escape ] with backslash)
        "\\]";
        return Pattern.compile(
            pattern,
            flags);
    }

    public final boolean isDoQuickCheck() {
        return doQuickCheck;
    }

    public final void setDoQuickCheck(final boolean doQuickCheck) { 
        this.doQuickCheck = doQuickCheck;
    }
}