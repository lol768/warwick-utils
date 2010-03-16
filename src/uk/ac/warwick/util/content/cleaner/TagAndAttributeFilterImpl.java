package uk.ac.warwick.util.content.cleaner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;

public final class TagAndAttributeFilterImpl implements TagAndAttributeFilter {

	private static final Pattern ALIGN_STYLE = Pattern.compile("\\s*text-align:\\s*[a-z]+;?\\s*", Pattern.CASE_INSENSITIVE);
	
    private static final Set<String> disallowedTags = CleanerWriter.toSet(new String[] { "u", "font", "placetype", "placename",
            "place", "city", "country-region", "time", "date", "notextile", "stockticker", "personname",
            "shapetype", "stroke", "formulas", "f", "path", "lock", "shape", "imagedata"});
    
    private static final Set<String> disallowedNoAttributesTags = CleanerWriter.toSet(new String[] { "span" });

    private static final Set<String> disallowedAttributesAllTags = CleanerWriter.toSet(new String[] { "mce_keep", "_mce_keep", "onerror", "onsuccess", "onfailure" });

    private static final Set<String> allowedEmptyAttributes = CleanerWriter.toSet(new String[] { "alt" });

    private static final Set<String> disallowNested = CleanerWriter.toSet(new String[] { "b", "i", "strong", "em", "p", "sup", "sub",
            "script", "code", "pre", "a", "form" });
    
    /** UTL-72 Attributes that are not allowed to be set to themselves, e.g. background="background" */
    private static final Set<String> disallowedSelfAttributes = CleanerWriter.toSet(new String[] {
            "background", "width", "height", "src", "href", "colspan", "align"
    });

    private static final Map<String, Set<String>> disallowedAttributes;

    private Stack<String> removedNestedTags = new Stack<String>();
    
    private boolean allowJavascriptHandlers = true;
    
    static {
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
        allowed &= isAllowedAttributeForTag(tagName, attributeName, attributeValue);
        allowed &= isAllowedAttributeForAllTags(attributeName);

        // [SBTWO-1024] Don't allow empty attributes
        allowed &= isAllowedBlankAttribute(attributeName, attributeValue);
        
        // [UTL-72] Don't allow attributes set to themselves
        allowed &= isAllowedAttributeValue(attributeName, attributeValue);
        
        // [SBTWO-1090] Unwanted class markup
        if (attributeName.equalsIgnoreCase("class")) {
            allowed &= isAllowedClassName(tagName, attributeValue);
        }
        
        // [UTL-72] Unwanted styles
        if (attributeName.equalsIgnoreCase("style")) {
            allowed &= isAllowedStyle(tagName, attributeValue);
        }
        
        return allowed;
    }
    
    private boolean isAllowedClassName(final String tagName, final String className) {
        if (className.startsWith("mce") || className.startsWith("_mce") || className.startsWith("Mso")) {
            return false;
        } else if (tagName.equalsIgnoreCase("span") && className.matches("style\\d+")) {
            return false;
        }
        return true;
    }
    
    private boolean isAllowedStyle(final String tagName, final String style) {
        if (style.indexOf("url(background)") != -1) {
            return false;
        }
        
        return true;
    }

    private boolean isAllowedAttributeValue(final String attributeName, final String attributeValue) {
        if (disallowedSelfAttributes.contains(attributeName.toLowerCase()) && attributeName.equalsIgnoreCase(attributeValue)) {
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
    		if (attribute.equalsIgnoreCase(attributeName) || ("mce_" + attribute).equalsIgnoreCase(attributeName) || ("_mce_" + attribute).equalsIgnoreCase(attributeName)) {
    			return false;
    		}
    	}
        
        if (!isAllowJavascriptHandlers() && attributeName.toLowerCase().startsWith("on")) {
        	return false;
        }
        
        return true;
    }

    private boolean isAllowedAttributeForTag(final String tagName, final String attributeName, final String attributeValue) {
    	
    	// Specifically allow style if it's for alignment, even if we otherwise don't allow style
    	if (isAlignStyle(tagName, attributeValue)) {
    		return true;
    	}
    	
        for (String tag: disallowedAttributes.keySet()) {
            if (tagName.equals(tag)) {
                for (String attribute: disallowedAttributes.get(tag)) {
                	// do a little bit of fudging to look for mce_ attributes too
                    if (attributeName.equals(attribute) || attributeName.equals("mce_" + attribute) || attributeName.equals("_mce_" + attribute)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

	private boolean isAlignStyle(final String tagName, final String attributeValue) {
		return !tagName.equals("span") && ALIGN_STYLE.matcher(attributeValue).matches();
	}

    private boolean isTagAllowed(final String tagName, final boolean hasAtts, final boolean closingTag) {
        boolean result = true;
        if (disallowedTags.contains(tagName)) {
            result = false;
        }
        if (!hasAtts && !closingTag) {
            if (disallowedNoAttributesTags.contains(tagName)) {
                result = false;
            }
        }
        
        return result;
    }

    public boolean isTagAllowed(final String tagName, final Stack<String> tagStack, final boolean isClosingTag, final Attributes atts) {
        boolean hasAttributes = hasAttributes(tagName, atts);
        
        boolean allowed = isTagAllowed(tagName, hasAttributes, isClosingTag);

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
