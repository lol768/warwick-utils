package uk.ac.warwick.util.content.texttransformers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import uk.ac.warwick.util.content.MutableContent;

public abstract class AbstractMagicTagTransformer implements TextTransformer {
    
    private final String[] tagNames;
    private final Pattern tagPattern;

    /**
     * Whether to perform a quick indexOf to bail-out early. You would only
     * want to disable this if you are doing it differently yourself, and don't want
     * the additional overhead of it happening twice.
     */
    private boolean doQuickCheck = true;
    
    protected AbstractMagicTagTransformer(String[] theTagNames, Pattern theTagPattern) {
        this.tagNames = theTagNames;
        this.tagPattern = theTagPattern;
    }
    
    /**
     * Deliberately not final, allow to be overridden
     */
    public boolean applies(MutableContent mc) {
        String html = mc.getContent();
        boolean found = false;
        
        for (String tagName : tagNames) {
            if (html.toLowerCase().indexOf(("[" + tagName).toLowerCase()) != -1) {
                found = true;
            }
        }
        
        if (!found) {
            return false;
        }
        
        return getTagPattern().matcher(html).find();
    }
    
    public final MutableContent apply(final MutableContent mc) {
        //Quick escape
        if (doQuickCheck && !applies(mc)) {
            return mc;
        }
        
        String html = mc.getContent();
        
        //we need to split HTML into do and don't do...
        Pattern noTextile = Pattern.compile("<notextile>(.*?)</notextile>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = noTextile.matcher(html);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;
        
        List<String> heads = Lists.newArrayList();
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            
            String transformed = TextPatternTransformer.transform(html.substring(lastMatch, startIndex), getTagPattern(), getCallback(), mc, !isTagGeneratesHead());
            sb.append(extractHeads(transformed, heads));
            sb.append(html.substring(startIndex, endIndex));
            lastMatch = endIndex;
        }
        
        String transformed = TextPatternTransformer.transform(html.substring(endIndex), getTagPattern(), getCallback(), mc, !isTagGeneratesHead());
        sb.append(extractHeads(transformed, heads));
        
        if (heads.isEmpty()) {
            mc.setContent(sb.toString());
        } else {
            String output = sb.toString();
            for (String head : heads) {
                output = injectHead(output, head);
            }
            
            mc.setContent(output);
        }
        
        return mc;
    }
    
    public final <T> List<T> collect(final MutableContent mc, final Function<String, T> function) {
        if (!applies(mc)) {
            return Collections.emptyList();
        }
        
        List<T> transformed = Lists.newArrayList();
        
        String html = mc.getContent();
        
        Pattern noTextile = Pattern.compile("<notextile>(.*?)</notextile>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = noTextile.matcher(html);
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            
            transformed.addAll(TextPatternTransformer.collect(html.substring(lastMatch, startIndex), getTagPattern(), function));

            lastMatch = endIndex;
        }
        
        transformed.addAll(TextPatternTransformer.collect(html.substring(endIndex), getTagPattern(), function));
        
        return transformed;
    }
    
    static String extractHeads(String html, List<String> heads) {
        if (html.indexOf("<head") == -1) {
            return html;
        }
        
        Matcher m = HEAD_MATCHER.matcher(html);
        
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;
        while (m.find()) {
            startIndex = m.start();
            endIndex = m.end();
            
            sb.append(html.substring(lastMatch, startIndex).trim());
            
            heads.add(m.group(1));
            
            lastMatch = endIndex;
        }
        
        sb.append(html.substring(endIndex).trim());
        
        String transformed = sb.toString();
        
        String body = TextPatternTransformer.getTagAndContents(transformed, "body");
        if (body == null) {
            return transformed;
        } else {
            return body;
        }
    }
    
    private String injectHead(String originalHtml, String head) {
        String html = originalHtml;
        
        if (!HTMLSTART.matcher(html).find()) {
            //if originalHtml is just body, wrap it in tags
            html = "<html><head>" + head +"</head><body>" + 
                        html + 
                        "</body></html>";
        } else if (!HEADEND.matcher(html).find()) {
            //if there is html but no head, add a head
            html = HTMLSTART.matcher(html).replaceFirst("$1<head>"+head.replace("$", "\\$")+"</head>");
        } else {
            html = HEADEND.matcher(html).replaceFirst(head.replace("$", "\\$")+"$1");
        }
        return html;
    }

    protected final Map<String, Object> extractParameters(final String string) {
        if (string == null) {
            return new HashMap<String, Object>();
        }
        Map<String,Object> result = new HashMap<String,Object>();
        AttributeStringParser parser = new AttributeStringParser(string);
        List<Attribute> attributes = parser.getAttributes();
        for (Attribute a : attributes) {
            String name = a.getName().toLowerCase();
            
            if (getAllowedParameters() == null) {
                result.put(name, a.getValue());
                continue;
            }
            
            for (String allowedParameter : getAllowedParameters()) {
                if (name.equals(allowedParameter)) {
                    result.put(name, a.getValue());
                }
            }
        }
        return result;
    }
    
    public final Pattern getTagPattern() {
        return tagPattern;
    }
    
    protected abstract boolean isTagGeneratesHead();
    
    protected abstract String[] getAllowedParameters();
 
    /**
     * The Callback which is given matching tags and expected to return
     * the transformed result.
     */
    protected abstract TextPatternTransformer.Callback getCallback();

    public final boolean isDoQuickCheck() {
        return doQuickCheck;
    }

    public final void setDoQuickCheck(final boolean doQuickCheck) { 
        this.doQuickCheck = doQuickCheck;
    }

}
