package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.web.Uri;

/**
 * Converts relative links to absolute links given a base.
 */
public final class RelativeLinkTextTransformer implements TextTransformer {

    private static final int MATCH_OUTRO = 3;

    private static final int MATCH_INTRO = 1;

    private static final int MATCH_URL = 2;

    private static final String MATCH_HREF_QUOTES = "(\\shref=\")([^\"]+)(\")";

    private static final String MATCH_HREF_QUOTE = "(\\shref=')([^']+)(')";

    private static final String MATCH_SRC_QUOTES = "(\\ssrc=\")([^\"]+)(\")";

    private static final String MATCH_SRC_QUOTE = "(\\ssrc=')([^']+)(')";

    private static final String MATCH_HREF_NOQUOTES = "(\\shref=)([^\"'\\s>]+)()";

    private static final String MATCH_SRC_NOQUOTES = "(\\ssrc=)([^\"'\\s>]+)()";

    private static final String[] PATTERNS = new String[] { MATCH_HREF_QUOTES, MATCH_HREF_QUOTE, MATCH_HREF_NOQUOTES,
            MATCH_SRC_QUOTES, MATCH_SRC_QUOTE, MATCH_SRC_NOQUOTES };

    private final Uri base;

    public RelativeLinkTextTransformer(final Uri theBase) {
        this.base = theBase;
    }
    
    public RelativeLinkTextTransformer(final String theBase) {
        this(Uri.parse(theBase));
    }
    
    public String transform(final String text) {
    	// parse out nasty script tags
    	Pattern noScriptTags = Pattern.compile("<script([^>]*)>(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        
        Matcher matcher = noScriptTags.matcher(text);
        StringBuilder sb = new StringBuilder();
        
        int lastMatch = 0;
        int startIndex = 0;
        int endIndex = 0;    
        
        while (matcher.find()) {
            startIndex = matcher.start();
            endIndex = matcher.end();
            sb.append(doUrlRewriting(text.substring(lastMatch, startIndex)));
            
            if (matcher.group(1).contains("src=")) {
            	// do URL rewriting on the script tag anyway
            	sb.append(doUrlRewriting("<script" + matcher.group(1) + ">"));
            	sb.append(matcher.group(2));
            	sb.append("</script>");
            } else {
            	// don't rewrite inside script tags
            	sb.append(text.substring(startIndex, endIndex));
            }
            
            lastMatch = endIndex;
        }
        
        sb.append(doUrlRewriting(text.substring(endIndex)));
        
        return sb.toString();
    }

    public String doUrlRewriting(final String text) {
        String result = text;

        for (String pattern: PATTERNS) {
            result = doTransform(result, pattern);
        }

        return result;
    }

    private String doTransform(final String text, final String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        StringBuffer sb = new StringBuffer();

        Matcher m = p.matcher(text);

        while (m.find()) {
            String url = m.group(MATCH_URL);

            url = parseUrl(url).toString();

            m.appendReplacement(sb, m.group(MATCH_INTRO) + url + m.group(MATCH_OUTRO));
        }

        m.appendTail(sb);

        return sb.toString();
    }

    private Uri parseUrl(final String url) {
        // take the URL and, if necessary, absolute it
        return base.resolve(Uri.parse(url));
    }

}
