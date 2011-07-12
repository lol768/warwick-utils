package uk.ac.warwick.util.content.texttransformers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.core.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
    
    /**
     * If this is true, the pattern will generate <head> content, so it can safely be ignored.
     */
    protected abstract boolean isGeneratesHead();
    
    public final MutableContent apply(MutableContent mc) {
        String text = mc.getContent();
        text = transform(text, getPattern(), getCallback(), mc, !isGeneratesHead());
        mc.setContent(text);
        return mc;
    }
    
    public static final String transform(final String theContent, final Pattern pattern, final Callback callback, final MutableContent mc, boolean ignoreHead) {
        return alternateTransform(theContent, pattern, callback, new Callback() {
            public String transform(String input, MutableContent mc) {
                return input;
            }
        }, mc, ignoreHead);
    }
    
    public static final <T> List<T> collect(final String content, final Pattern pattern, final Function<String, T> fn) {
        Matcher matcher = pattern.matcher(content);
        
        List<T> transformed = Lists.newArrayList();
        
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
        
            transformed.add(fn.apply(content.substring(startIndex, endIndex)));
        }
        
        return transformed;
    }
    
    /**
     * This method is very similar to transform(), except it provides
     * a second Callback which is used on all text NOT matching the
     * pattern.
     */
    public static final String alternateTransform(final String theContent, final Pattern pattern, final Callback innerCallback, final Callback outerCallback, final MutableContent mc, boolean ignoreHead) {
        Matcher matcher = pattern.matcher(theContent);
        StringBuilder sb = new StringBuilder();
        StringBuilder allHead = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(transform(outerCallback, theContent.substring(lastMatch, startIndex), mc, allHead, ignoreHead));
            sb.append(transform(innerCallback, theContent.substring(startIndex, endIndex), mc, allHead, ignoreHead));
            
            lastMatch = endIndex;
        }
        
        sb.append(transform(outerCallback, theContent.substring(endIndex), mc, allHead, ignoreHead));
        
        String html = sb.toString();
        if (!ignoreHead && allHead.length() > 0) {
            String head = allHead.toString();
            
            // we need to inject some head
            if (!HTMLSTART.matcher(html).find()) {
                //if originalHtml is just body, wrap it in tags
                html = "<html><head>" + head +"</head><body>" + 
                            html + 
                            "</body></html>";
            } else if (!HEADEND.matcher(html).find()) {
                //if there is html but no head, add a head
                html = HTMLSTART.matcher(html).replaceFirst("$1<head>"+Matcher.quoteReplacement(head)+"</head>");
            } else {
                html = HEADEND.matcher(html).replaceFirst(Matcher.quoteReplacement(head)+"$1");
            }
        }
        
        return html;
    }
    
    private static String transform(Callback callback, String string, MutableContent mc, StringBuilder allHead, boolean ignoreHead) {
        String transformed = callback.transform(string, mc); 
        
        if (ignoreHead) {
            return transformed;
        }
        
        String body = getTagAndContents(transformed, "body");
        if (body == null) {
            return transformed;
        } else {            
            String head = getTagAndContents(transformed, "head");
            if (head != null) {
                allHead.append(head);
            }
            
            return body;
        }
    }
    
    public static interface Callback {
        String transform(final String input, final MutableContent mc);
    }
    
    private static final char CLOSING_ANGLE_BRACKET = '>';
    private static final char OPENING_ANGLE_BRACKET = '<';
    private static final char TERMINATING_SLASH = '/';
    
    public static String getTagAndContents(final String thehtml, final String tagToFind) {
        int nextAngleBracket = thehtml.indexOf(OPENING_ANGLE_BRACKET);
        if (nextAngleBracket != -1) {
            int startOfBody = -1;
            int endOfBody = -1;
            boolean foundBothStartAndEnd = false;
            while (nextAngleBracket != -1 && !foundBothStartAndEnd) {
                String tag;
                // this might be the start tag or the first character of the ending tag
                char maybeTerminatingSlash = thehtml.charAt(nextAngleBracket + 1);
                tag = getPotentialTag(thehtml, nextAngleBracket, maybeTerminatingSlash, tagToFind);

                if (tagToFind.equalsIgnoreCase(tag)) {
                    int nextCloseBracket = thehtml.indexOf(CLOSING_ANGLE_BRACKET, nextAngleBracket + tag.length());

                    if (maybeTerminatingSlash != TERMINATING_SLASH) {
                        startOfBody = nextCloseBracket + 1;
                    } else {
                        endOfBody = nextAngleBracket;
                    }
                }
                foundBothStartAndEnd = startOfBody > -1 && endOfBody > -1;
                nextAngleBracket = thehtml.indexOf(OPENING_ANGLE_BRACKET, nextAngleBracket + 1);
            }

            return retrieveText(thehtml, startOfBody, endOfBody, foundBothStartAndEnd);
        }
        
        // This document has no HTML tags in it whatsoever. We should handle this elsewhere.
        return null;
    }

    private static String getPotentialTag(final String text, final int nextAngleBracket, final char maybeTerminatingSlash, final String tag) {
        int start = 0;
        int end = 0;
        if (maybeTerminatingSlash == TERMINATING_SLASH) {
            start = nextAngleBracket + 2;
            end = nextAngleBracket + tag.length() + 2;
        } else {
            start = nextAngleBracket + 1;
            end = nextAngleBracket + tag.length() + 1;
        }
        return StringUtils.safeSubstring(text, start, end);
    }

    /**
     * If we have a start, return from start until end tag (or end if no end tag)
     * If we have no start but we do have an end, return from 0 to end tag
     * If we have no start and no end tag, return nothing.
     */
    private static String retrieveText(final String text, final int startOfBody, final int endOfBody, final boolean foundBothStartAndEnd) {
        String result;
        // start tag is after end tag.  Screwy data
        if (startOfBody > endOfBody && startOfBody != -1 && endOfBody != -1) {
            result = null;
        } else if (startOfBody == -1 && endOfBody == -1) {
            // we haven't found either a start nor an end.            
            // found neither a start nor an end.
            result = null; 
        } else {
            if (foundBothStartAndEnd) {
                result = text.substring(startOfBody, endOfBody);
            } else if (startOfBody > -1) {
                result = text.substring(startOfBody);
            } else if (endOfBody > -1) {
                result = text.substring(0, endOfBody);
            } else {
                result = text;
            }
        }
        return result;
    }
}
