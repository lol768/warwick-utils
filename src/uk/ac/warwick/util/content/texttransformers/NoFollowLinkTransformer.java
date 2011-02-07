package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

/**
 * Add rel="nofollow" to any links that we have
 * 
 * @author Mat Mannion
 */
public class NoFollowLinkTransformer implements TextTransformer {
	
	private static final Pattern MATCH_PATTERN = Pattern.compile("(\\<a\\s)(.+\\>)", Pattern.CASE_INSENSITIVE);
	
	public MutableContent apply(MutableContent mc) {
	    String content = mc.getContent();
    	Matcher matcher = MATCH_PATTERN.matcher(content);
    	StringBuffer sb = new StringBuffer();
    	
    	while (matcher.find()) {
    		matcher.appendReplacement(sb, matcher.group(1) + "rel=\"nofollow\" " + matcher.group(2));
    	}
    	
    	matcher.appendTail(sb);
		
		mc.setContent(sb.toString());
		return mc;
    }
}
