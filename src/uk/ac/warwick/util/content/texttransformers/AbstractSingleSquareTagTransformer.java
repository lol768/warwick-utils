package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

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
 
    protected abstract TextPatternTransformer.Callback getCallback();
    
    protected abstract String[] getAllowedParameters();
    
    public final String transform(final String text) {
        String html = text;
        //Quick escape
        if (doQuickCheck && html.toLowerCase().indexOf(("[" + tagName).toLowerCase()) == -1) {
            return html;
        }
        try {
            html = new TagTransformer().transform(html, getCallback());
        } catch (Exception e) {
            LOGGER.error("Caught exception trying to run square bracket", e);
        }
        return html;
    }
    
    public final Pattern getTagPattern() {
        return tagPattern;
    }
    
    /**
     * The class which goes through the input, matching patterns
     * and using the callback to return the transformed text.
     */
    class TagTransformer extends TextPatternTransformer {
        protected Pattern getPattern() {
            return getTagPattern();
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