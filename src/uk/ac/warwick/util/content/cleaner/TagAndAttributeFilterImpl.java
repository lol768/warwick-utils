package uk.ac.warwick.util.content.cleaner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;

public final class TagAndAttributeFilterImpl implements TagAndAttributeFilter {

    private final Set<String> disallowedTags = CleanerWriter.toSet(new String[] { "u", "font", "placetype", "placename",
            "place", "city", "country-region", "time", "date", "notextile" });
    
    private final Set<String> disallowedNoAttributesTags = CleanerWriter.toSet(new String[] { "span" });

    private final Set<String> disallowedAttributesAllTags = CleanerWriter.toSet(new String[] { "mce_keep", "onerror", "onsuccess", "onfailure" });

    private final Set<String> allowedEmptyAttributes = CleanerWriter.toSet(new String[] { "alt" });

    private final Set<String> disallowNested = CleanerWriter.toSet(new String[] { "b", "i", "strong", "em", "p", "sup", "sub",
            "script", "code", "pre", "a", "form" });

    private final Map<String, Set<String>> disallowedAttributes;

    private Stack<String> removedNestedTags = new Stack<String>();
    
    private boolean allowJavascriptHandlers = true;

	public TagAndAttributeFilterImpl() {
        disallowedAttributes = new HashMap<String, Set<String>>();
        //disallowedAttributes.put("div", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("span", CleanerWriter.toSet(new String[] { "style", "lang" }));
        disallowedAttributes.put("h1", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("h2", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("h3", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("h4", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("h5", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("h6", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("b", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("i", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("p", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("strong", CleanerWriter.toSet(new String[] { "style" }));
        disallowedAttributes.put("em", CleanerWriter.toSet(new String[] { "style" }));
    }

    public boolean isAttributeAllowed(final String tagName, final String attributeName) {
        return isAttributeAllowed(tagName, attributeName, "");
    }

    public boolean isAttributeAllowed(final String tagName, final String attributeName, final String attributeValue) {
        boolean allowed = true;
        allowed &= isAllowedAttributeForTag(tagName, attributeName);

        allowed &= isAllowedAttributeForAllTags(attributeName);

        // [SBTWO-1024] Don't allow empty attributes
        allowed &= isAllowedBlankAttribute(attributeName, attributeValue);
        
        // [SBTWO-1090] Unwanted class markup
        if (attributeName.equalsIgnoreCase("class")) {
            allowed &= isAllowedClassName(attributeValue);
        }
        
        return allowed;
    }
    
    private boolean isAllowedClassName(final String className) {
        if (className.startsWith("mce") || className.startsWith("Mso")) {
            return false;
        }
        return true;
    }

    private boolean isAllowedBlankAttribute(final String attributeName, final String attributeValue) {
        boolean canBeBlank = (allowedEmptyAttributes.contains(attributeName));
        if (attributeValue.equals("") && !canBeBlank) {
            return false;
        }
        return true;
    }

    private boolean isAllowedAttributeForAllTags(final String attributeName) {
    	for (String attribute : disallowedAttributesAllTags) {
    		if (attribute.equalsIgnoreCase(attributeName) || ("mce_" + attribute).equalsIgnoreCase(attributeName)) {
    			return false;
    		}
    	}
        
        if (!isAllowJavascriptHandlers() && attributeName.toLowerCase().startsWith("on")) {
        	return false;
        }
        
        return true;
    }

    private boolean isAllowedAttributeForTag(final String tagName, final String attributeName) {
        for (String tag: disallowedAttributes.keySet()) {
            if (tagName.equals(tag)) {
                for (String attribute: disallowedAttributes.get(tag)) {
                	// do a little bit of fudging to look for mce_ attributes too
                    if (attributeName.equals(attribute) || attributeName.equals("mce_" + attribute)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isTagAllowed(final String tagName, final boolean hasAttsOrClosing) {
        boolean result = true;
        if (disallowedTags.contains(tagName)) {
            result = false;
        }
        if (!hasAttsOrClosing) {
            if (disallowedNoAttributesTags.contains(tagName)) {
                result = false;
            }
        }
        return result;
    }

    public boolean isTagAllowed(final String tagName, final Stack<String> tagStack, final boolean isClosingTag, final Attributes atts) {
        boolean hasAttributes = hasAttributes(tagName, atts);
        
        boolean allowed = isTagAllowed(tagName, hasAttributes || isClosingTag);

        if (disallowNested.contains(tagName)
                && ((isClosingTag && !removedNestedTags.isEmpty() && removedNestedTags.pop().equals(tagName)) || (!isClosingTag && tagStack
                        .contains(tagName)))) {
            allowed = false;
            if (!isClosingTag) {
                removedNestedTags.push(tagName);
            }
        }

        return allowed;
    }
    
    private boolean hasAttributes(final String tagName, final Attributes atts) {
        if (atts == null) {
            return false;
        }
        
        boolean result = false;
        
        int attCount = atts.getLength();
        if (attCount > 0) {
            for (int i=0;i<attCount;i++) {
                String attName = atts.getLocalName(i);
                String value = atts.getValue(i);
                
                if (isAttributeAllowed(tagName, attName, value)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    public boolean isAllowJavascriptHandlers() {
		return allowJavascriptHandlers;
	}

	public void setAllowJavascriptHandlers(boolean allowJavascriptHandlers) {
		this.allowJavascriptHandlers = allowJavascriptHandlers;
	}
}
