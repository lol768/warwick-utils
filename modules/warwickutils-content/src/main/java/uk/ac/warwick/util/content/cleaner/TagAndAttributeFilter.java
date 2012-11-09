package uk.ac.warwick.util.content.cleaner;

import java.util.Stack;

import org.xml.sax.Attributes;

public interface TagAndAttributeFilter {

    /**
     * Check whether a tag is allowed, but also check if it is allowed to be
     * nested and if not, check whether it is within itself.
     */
    boolean isTagAllowed(String tagName, Stack<String> tagStack, boolean isClosingTag, Attributes atts);

    /**
     * Check whether an attribute is allowed in this tag.
     */
    boolean isAttributeAllowed(String tagName, String attributeName);

    /**
     * Check whether an attribute is allowed in this tag with this value.
     */
    boolean isAttributeAllowed(String tagName, String attributeName, String attributeValue);
    
    /**
     * Set whether to allow on* attributes
     */
    void setAllowJavascriptHandlers(boolean allow);
}