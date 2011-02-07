package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

/**
 * A class which transforms text using three things:
 *  - source string
 *  - regex pattern that matches text to be changed
 *  - a callback object that decides what to change matching text to
 *  
 * Note this is separate from the regular TextTransformer.
 */
public abstract class TextPatternTransformer implements TextTransformer {
    
    protected abstract Pattern getPattern(); 
    
    protected abstract Callback getCallback();
    
    public final MutableContent apply(MutableContent mc) {
        String text = mc.getContent();
        text = transform(text, getPattern(), getCallback(), mc);
        mc.setContent(text);
        return mc;
    }
    
    public static final String transform(final String theContent, final Pattern pattern, final Callback callback, final MutableContent mc) {
        Matcher matcher = pattern.matcher(theContent);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(theContent.substring(lastMatch, startIndex));
            sb.append(callback.transform(theContent.substring(startIndex, endIndex), mc));
            lastMatch = endIndex;
        }
        
        sb.append(theContent.substring(endIndex));
        return sb.toString();
    }
    
    /**
     * This method is very similar to transform(), except it provides
     * a second Callback which is used on all text NOT matching the
     * pattern.
     */
    public static final String alternateTransform(final String theContent, final Pattern pattern, final Callback innerCallback, final Callback outerCallback, final MutableContent mc) {
        Matcher matcher = pattern.matcher(theContent);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(outerCallback.transform(theContent.substring(lastMatch, startIndex), mc));
            sb.append(innerCallback.transform(theContent.substring(startIndex, endIndex), mc));
            lastMatch = endIndex;
        }
        
        sb.append(outerCallback.transform(theContent.substring(endIndex), mc));
        return sb.toString();
    }
    
    public static interface Callback {
        String transform(final String input, final MutableContent mc);
    }
}
