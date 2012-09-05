package uk.ac.warwick.util.content.texttransformers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.core.StringUtils;

/**
 * Really quite similar to AbstractSquareTagTransformer, but only looks for
 * single tags, and so naturally doesn't check for tag contents.
 */
public abstract class AbstractSingleSquareTagTransformer extends AbstractMagicTagTransformer {
    
    public static final int PARAMETERS_MATCH_GROUP = 1;
    
    public AbstractSingleSquareTagTransformer(final String... theTagNames) {
        super(theTagNames, getPatternForTagName(theTagNames));
    }
    
    private static Pattern getPatternForTagName(final String... theTagName) {
        int flags = Pattern.CASE_INSENSITIVE;       
        String pattern = "\\[(?:" + StringUtils.join(theTagName,"|") + ")" +      //opening tag
        "(\\s+.+?[^\\\\])?" +          //optional parameters (can escape ] with backslash)
        "\\]";
        return Pattern.compile(
            pattern,
            flags);
    }
    
    protected final Map<String, Object> getParameters(Matcher matcher) {
        return extractParameters(matcher.group(PARAMETERS_MATCH_GROUP));
    }
}