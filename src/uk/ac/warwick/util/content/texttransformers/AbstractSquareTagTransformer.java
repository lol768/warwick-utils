package uk.ac.warwick.util.content.texttransformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.core.StringUtils;

import com.google.common.collect.Lists;

/**
 * Abstract tag transformer which works upon 
 * 
 * @author cusebr
 */
public abstract class AbstractSquareTagTransformer implements TextTransformer {
    
	private static final int TAGNAME_MATCH_GROUP = 1;
    private static final int PARAMETERS_MATCH_GROUP = 2;
    private static final int CONTENTS_MATCH_GROUP = 3;
    
    private static final int BLOCKLEVEL_STANDARD_TAGNAME_MATCH_GROUP = 2;
    private static final int BLOCKLEVEL_STANDARD_PARAMETERS_MATCH_GROUP = 3;
    private static final int BLOCKLEVEL_STANDARD_CONTENTS_MATCH_GROUP = 4;
    
    private static final int BLOCKLEVEL_BLOCK_TAGNAME_MATCH_GROUP = 6;
    private static final int BLOCKLEVEL_BLOCK_PARAMETERS_MATCH_GROUP = 7;
    private static final int BLOCKLEVEL_BLOCK_CONTENTS_MATCH_GROUP = 8;
    
    private final Pattern tagPattern; 
    
    private final String[] tagNames;
    
    private final boolean isBlockLevel;
    
    /**
     * Whether to perform a quick indexOf to bail-out early. You would only
     * want to disable this if you are doing it differently yourself, and don't want
     * the additional overhead of it happening twice.
     */
    private boolean doQuickCheck = true;
    
    public AbstractSquareTagTransformer(final String theTagName) {
        this(theTagName, false);
    }
    
    public AbstractSquareTagTransformer(final String theTagName, final boolean multiline) {
        this(theTagName, multiline, false);
    }
    
    public AbstractSquareTagTransformer(final String theTagName, final boolean multiline, final boolean allowEmpty) {
        this(theTagName, multiline, allowEmpty, false);
    }
    
    public AbstractSquareTagTransformer(final String theTagName, final boolean multiline, final boolean allowEmpty, final boolean blockLevel) {
        this(new String[] { theTagName }, multiline, allowEmpty, blockLevel);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames) {
        this(theTagNames, false);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames, final boolean multiline) {
        this(theTagNames, multiline, false);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames, final boolean multiline, final boolean allowEmpty) {
        this(theTagNames, multiline, allowEmpty, false);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames, final boolean multiline, final boolean allowEmpty, final boolean blockLevel) {
        this.isBlockLevel = blockLevel;
        
        this.tagPattern = getPatternForTagNames(theTagNames, multiline, allowEmpty, blockLevel);
        this.tagNames = theTagNames;
    }
 
    /**
     * The Callback which is given matching tags and expected to return
     * the transformed result.
     */
    protected abstract TextPatternTransformer.Callback getCallback();
    
    protected abstract String[] getAllowedParameters();
    
    public final MutableContent apply(final MutableContent mc) {
        String html = mc.getContent();
        
        //Quick escape
        if (doQuickCheck) {
        	boolean found = false;
        	
        	for (String tagName : tagNames) {
        		if (html.toLowerCase().indexOf(("[" + tagName).toLowerCase()) != -1) {
        			found = true;
        		}
        	}
        	
        	if (!found) {
        		return mc;
        	}
        }
        
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
            
            String transformed = TextPatternTransformer.transform(html.substring(lastMatch, startIndex), getTagPattern(), getCallback(), mc);
            sb.append(extractHeads(transformed, heads));
            sb.append(html.substring(startIndex, endIndex));
            lastMatch = endIndex;
        }
        
        String transformed = TextPatternTransformer.transform(html.substring(endIndex), getTagPattern(), getCallback(), mc);
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
        
        return sb.toString();
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
    
    private Pattern getPatternForTagNames(final String[] theTagNames, final boolean multiline, final boolean allowEmpty, final boolean blockLevel) {
        int flags = Pattern.CASE_INSENSITIVE;
        String optional = "";
        if (multiline) {
            flags |= Pattern.DOTALL;
        }
        if (allowEmpty) {
            optional = "?";
        }
        
        String pattern = "\\[(" + StringUtils.join(theTagNames,"|") + ")" +                //opening tag
        "(\\s+.+?[^\\\\])?" +          //optional parameters (can escape ] with backslash)
        "\\]" +         
        "(.+?)" + optional +               //url
        "\\[/\\1\\]";             //closing tag
        
        if (blockLevel) { //if we have a blocklevel element, remove TinyMCE's nasty <p> tag wrapping
            pattern = "(" 
            	+ pattern.replace("\\1", "\\" + BLOCKLEVEL_STANDARD_TAGNAME_MATCH_GROUP) 
            	+ ")|((?:<p>\\s*(?:&nbsp;)*\\s*)" 
            	+ pattern.replace("\\1", "\\" + BLOCKLEVEL_BLOCK_TAGNAME_MATCH_GROUP) 
            	+ "(?:\\s*(?:&nbsp;)*\\s*</p>))";
        }
        
        return Pattern.compile(
            pattern,
            flags);
    }
    
    protected final Map<String, Object> getParameters(Matcher matcher) {
        if (isBlockLevel) {
            Map<String,Object> result;
            
            if (matcher.group(1) != null) {
                // normal grouping
                result = extractParameters(matcher.group(BLOCKLEVEL_STANDARD_PARAMETERS_MATCH_GROUP));
            } else {
                // block level grouping
                result = extractParameters(matcher.group(BLOCKLEVEL_BLOCK_PARAMETERS_MATCH_GROUP));
            }
            
            return result;
        }
        
        return extractParameters(matcher.group(PARAMETERS_MATCH_GROUP));
    }
    
    protected String getTagName(Matcher matcher) {
        if (isBlockLevel) {
            String result;
            
            if (matcher.group(1) != null) {
                // normal grouping
                result = matcher.group(BLOCKLEVEL_STANDARD_TAGNAME_MATCH_GROUP);
            } else {
                // block level grouping
                result = matcher.group(BLOCKLEVEL_BLOCK_TAGNAME_MATCH_GROUP);
            }
            
            return result;
        }
        
        return matcher.group(TAGNAME_MATCH_GROUP);
    }
    
    protected final String getContents(Matcher matcher) {
        if (isBlockLevel) {
            String result;
            
            if (matcher.group(1) != null) {
                // normal grouping
                result = matcher.group(BLOCKLEVEL_STANDARD_CONTENTS_MATCH_GROUP);
            } else {
                // block level grouping
                result = "<p>" + matcher.group(BLOCKLEVEL_BLOCK_CONTENTS_MATCH_GROUP) + "</p>";
            }
            
            return result;
        }
        
        return matcher.group(CONTENTS_MATCH_GROUP);
    }

    public final boolean isDoQuickCheck() {
        return doQuickCheck;
    }

    public final void setDoQuickCheck(final boolean doQuickCheck) { 
        this.doQuickCheck = doQuickCheck;
    }
}