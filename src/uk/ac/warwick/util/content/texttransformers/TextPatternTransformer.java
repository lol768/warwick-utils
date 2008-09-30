package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class which transforms text using three things:
 *  - source string
 *  - regex pattern that matches text to be changed
 *  - a callback object that decides what to change matching text to
 *  
 * Note this is separate from the regular TextTransformer.
 */
public abstract class TextPatternTransformer {
    
    protected abstract Pattern getPattern(); 
    
    public final String transform(final String theContent, final Callback callback) {
        Matcher matcher = getPattern().matcher(theContent);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(theContent.substring(lastMatch, startIndex));
            sb.append(callback.transform(theContent.substring(startIndex, endIndex)));
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
    public final String alternateTransform(final String theContent, final Callback innerCallback, final Callback outerCallback) {
        Matcher matcher = getPattern().matcher(theContent);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(outerCallback.transform(theContent.substring(lastMatch, startIndex)));
            sb.append(innerCallback.transform(theContent.substring(startIndex, endIndex)));
            lastMatch = endIndex;
        }
        
        sb.append(outerCallback.transform(theContent.substring(endIndex)));
        return sb.toString();
    }
    
    public static interface Callback {
        String transform (final String input);
    }
}
