package uk.ac.warwick.util.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlUtils {
	
	/**
	 * Shared instance of pre-parsed HTML character entity references.
	 */
	private static final HtmlCharacterEntityReferences characterEntityReferences =
			new HtmlCharacterEntityReferences();
	
	/**
	 * Turn special characters into HTML character references.
	 * Handles complete character set defined in HTML 4.01 recommendation.
	 * <p>Escapes all special characters to their corresponding
	 * entity reference (e.g. <code>&lt;</code>).
	 * <p>Reference:
	 * <a href="http://www.w3.org/TR/html4/sgml/entities.html">
	 * http://www.w3.org/TR/html4/sgml/entities.html
	 * </a>
	 * @param input the (unescaped) input string
	 * @return the escaped string
	 */
	public static String htmlEscape(String input) {
		if (input == null) {
			return null;
		}
		StringBuffer escaped = new StringBuffer(input.length() * 2);
		for (int i = 0; i < input.length(); i++) {
			char character = input.charAt(i);
			String reference = characterEntityReferences.convertToReference(character);
			if (reference != null) {
				escaped.append(reference);
			}
			else {
				escaped.append(character);
			}
		}
		return escaped.toString();
	}
	
    public static Set<String> extractContentRegex(final String content, final String start, final String end) {
        Set<String> results = new LinkedHashSet<String>();
        Pattern p = Pattern.compile("(?ism)" + start + "(.*?)" + end);
        Matcher m = p.matcher(content);
        while (m.find()) {
            results.add(m.group(0));
        }
        m.replaceAll("");
        return results;
    }

    public static List<String> extractContent(final String content, final String start, final String end) {
        return extractContent(content, start, end, 0);
    }
    
    public static String replaceContent(final String parentContent, final String newContent, final String start, final String end) {      
        ContentFinder cf = new ContentFinder();
        cf.doFind(parentContent, start, end, 0);
        int startTagStart = cf.getStartTagStart();
        int startTagEnd = cf.getStartTagEnd();
        int endTagStart = cf.getEndTagStart();
        int endTagEnd = cf.getEndTagEnd();

        if (startTagStart == -1 || startTagEnd == -1 || endTagStart == -1 || endTagEnd == -1) {
            return newContent;
        }
        
        StringBuilder result = new StringBuilder();

        result.append(parentContent.substring(0,startTagEnd+1));
        result.append(newContent);
        result.append(parentContent.substring(endTagStart));
        
        return result.toString();
    }

    private static List<String> extractContent(final String content, final String start, final String end, final int startAtIndex) {
        List<String> results = new ArrayList<String>();
        
        String lowerContent = content.toLowerCase();
        
        ContentFinder cf = new ContentFinder();
        cf.doFind(content, start, end, startAtIndex);
        int startTagStart = cf.getStartTagStart();
        int startTagEnd = cf.getStartTagEnd();
        int endTagStart = cf.getEndTagStart();
        int endTagEnd = cf.getEndTagEnd();

        if (startTagStart == -1 || startTagEnd == -1 || endTagStart == -1 || endTagEnd == -1) {
            return results;
        }

//        if (keepSurroundingTags) {
//            extract = content.substring(startTagStart, endTagEnd + 1);
//        } else {
//            extract = content.substring(startTagEnd + 1, endTagStart);
//        }
        results.add(content.substring(startTagStart, endTagEnd + 1));

        if (lowerContent.indexOf(start, endTagEnd + 1) > 0) {
            results.addAll(extractContent(content, start, end, endTagEnd));
        }

        return results;
    }
    
    static class ContentFinder {
        private int startTagStart;
        private int startTagEnd;
        private int endTagStart;
        private int endTagEnd;
        
        ContentFinder() {
            resetIndices();
        }
        
        private void resetIndices() {
            startTagStart = -1;
            startTagEnd = -1;
            endTagStart = -1;
            endTagEnd = -1;
        }
        
        public int getEndTagEnd() {
            return endTagEnd;
        }
        public int getEndTagStart() {
            return endTagStart;
        }
        public int getStartTagEnd() {
            return startTagEnd;
        }
        public int getStartTagStart() {
            return startTagStart;
        }
        
        public void doFind(final String content, final String start, final String end, final int startAtIndex) {
            this.resetIndices();
            
            String lowerContent = content.toLowerCase();
            if (lowerContent.indexOf(start, startAtIndex) > -1) {
                startTagStart = lowerContent.indexOf(start, startAtIndex);
                startTagEnd = lowerContent.indexOf(">", startTagStart);
            }
            if (lowerContent.indexOf(end, startTagEnd) > startTagEnd) {
                endTagStart = lowerContent.indexOf(end, startTagEnd);
                endTagEnd = lowerContent.indexOf(">", endTagStart);
            }
        }
    }

}
