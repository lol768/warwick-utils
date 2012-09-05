package uk.ac.warwick.util.content.texttransformers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.core.StringUtils;

/**
 * Abstract tag transformer which works upon 
 * 
 * @author cusebr
 */
public abstract class AbstractSquareTagTransformer extends AbstractMagicTagTransformer {
    
	private static final int TAGNAME_MATCH_GROUP = 1;
    private static final int PARAMETERS_MATCH_GROUP = 2;
    private static final int CONTENTS_MATCH_GROUP = 3;
    
    private static final int BLOCKLEVEL_STANDARD_TAGNAME_MATCH_GROUP = 2;
    private static final int BLOCKLEVEL_STANDARD_PARAMETERS_MATCH_GROUP = 3;
    private static final int BLOCKLEVEL_STANDARD_CONTENTS_MATCH_GROUP = 4;
    
    private static final int BLOCKLEVEL_BLOCK_TAGNAME_MATCH_GROUP = 6;
    private static final int BLOCKLEVEL_BLOCK_PARAMETERS_MATCH_GROUP = 7;
    private static final int BLOCKLEVEL_BLOCK_CONTENTS_MATCH_GROUP = 8;
        
    private final boolean isBlockLevel;
    
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
    
    public AbstractSquareTagTransformer(final String... theTagNames) {
        this(theTagNames, false);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames, final boolean multiline) {
        this(theTagNames, multiline, false);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames, final boolean multiline, final boolean allowEmpty) {
        this(theTagNames, multiline, allowEmpty, false);
    }
    
    public AbstractSquareTagTransformer(final String[] theTagNames, final boolean multiline, final boolean allowEmpty, final boolean blockLevel) {
        super(theTagNames, getPatternForTagNames(theTagNames, multiline, allowEmpty, blockLevel));
        
        this.isBlockLevel = blockLevel;
    }
    
    private static Pattern getPatternForTagNames(final String[] theTagNames, final boolean multiline, final boolean allowEmpty, final boolean blockLevel) {
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
}