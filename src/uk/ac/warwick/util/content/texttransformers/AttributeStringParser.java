package uk.ac.warwick.util.content.texttransformers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AttributeStringParser {
    
    private static final Pattern NAME_EQUALS = Pattern.compile("^(\\s*(\\w+)=).+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern OPENER = Pattern.compile("^\\s*([\"']|\\&#8217\\;|\\&#8221\\;|\\&quot\\;)");
    private static final Pattern UNQUOTED = Pattern.compile("^[^\\s]+");
    
    private static final char ESCAPE = '\\'; 
    
    private final List<Attribute> attributes;
    
    public AttributeStringParser(final String attributesString) {

        attributes = new ArrayList<Attribute>();
        
        String text = attributesString.trim();
        
        /*
         * Parse through the string, looking first for 'name=', then
         * seeing if there's a quote character, then looking for a matching end quote (ignoring
         * where a quote has a slash before it). Repeat until there is no more string.
         * 
         * If no quote character is found, it just matches a word.
         * 
         * Here we progress through text by repeatedly calling substring(), though we could
         * have instead used the start index support of the pattern matchers. It's not much difference
         * either way... substring() uses the same backing bytes as the original string anyway.
         */
        Matcher matcher = NAME_EQUALS.matcher(text);
        while (matcher.find()) {
        	String openedQuote;
        	String name = matcher.group(2);
        	String value;
        	text = text.substring(matcher.group(1).length());
        	Matcher openMatcher = OPENER.matcher(text);
        	if (openMatcher.find()) {
        		// Quoted attribute
        		openedQuote = openMatcher.group(1);
        		text = text.substring(openMatcher.group().length());
        		int end = findEndOfValue(openedQuote, text);
        		if (end > -1) {
        			value = text.substring(0, end);
        		} else {
        			value = text;
        		}
        		//remove slashes from quotes
        		value = value.replace(ESCAPE + openedQuote, openedQuote);
        		text = text.substring(end + openedQuote.length());
        	} else {
        		//Unquoted attribute
        		Matcher unquotedMatcher = UNQUOTED.matcher(text);
        		value = "";
        		if (unquotedMatcher.find()) {
        			value = unquotedMatcher.group();
        		}
        		text = text.substring(unquotedMatcher.group().length());
        	}
        	attributes.add(new Attribute(name, value));
        	matcher = NAME_EQUALS.matcher(text);
        }
    }

    /**
     * Keep looking for a particular quote character until we find one
     * that doesn't have a backslash before it.
     * 
     * Does have the slightly strange behaviour that you can escape the
     * longer quotes like \&quot; - which might be good...?
     */
	private int findEndOfValue(String openedQuote, String text) {
		int start = 0;
		int end;
		while ((end = text.indexOf(openedQuote, start)) > -1) {
			start = end+1;
			if (end > 0 && text.charAt(end-1) == ESCAPE) {
				continue;
			} else {
				break;
			}
		}
		return end;
	}
    
    public List<Attribute> getAttributes() {
        return attributes;
    }
    
    public String getValue(final String attributeName) {
        for (Attribute a : attributes) {
            if (attributeName.equals(a.getName())) {
                return a.getValue();
            }
        }
        return null;
    }
}
