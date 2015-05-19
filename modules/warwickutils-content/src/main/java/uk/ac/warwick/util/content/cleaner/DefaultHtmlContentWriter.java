package uk.ac.warwick.util.content.cleaner;

import java.util.HashSet;
import java.util.Set;

import org.ccil.cowan.tagsoup.ElementType;
import org.ccil.cowan.tagsoup.Schema;
import org.xml.sax.Attributes;

import uk.ac.warwick.html5.HTML5Schema;
import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.core.HtmlUtils;

/**
 * Default implementation
 * 
 * @author Mat Mannion
 */
public class DefaultHtmlContentWriter implements HtmlContentWriter {

    public static final String[] MCE_TAGS = new String[] {"href","src","style","coords","shape"};
    
    private static final int MAX_ASCII_VALUE = 127;

    private final TagAndAttributeFilter filter;

    private final BodyContentFilter contentFilter;

    private final Schema schema = new HTML5Schema();

    public DefaultHtmlContentWriter(TagAndAttributeFilter tagAndAttributeFilter, BodyContentFilter cFilter) {
        this.filter = tagAndAttributeFilter;
        this.contentFilter = cFilter;
    }

    public String renderStartTag(String tagName, Attributes atts, MutableContent mc) {
        StringBuilder tagSb = new StringBuilder();

        tagSb.append("<" + tagName);
        tagSb.append(constructAttributes(atts, tagName));
        if (isSelfCloser(tagName)) {
            tagSb.append(" /");
        }
        tagSb.append(">");

        return tagSb.toString();
    }

    public String renderEndTag(String tagName) {
        return "</" + tagName + ">";
    }

    public boolean isSelfCloser(final String tagName) {
        ElementType type = schema.getElementType(tagName);
        if (type == null) {
            // Treat unknown tags as non-self closing
            return false;
        }
        
        return type.model() == Schema.M_EMPTY;
    }

    /**
     * Build a string of escape name="value" pairs from an Attributes object.
     */
    private StringBuilder constructAttributes(final Attributes atts, final String tagName) {
        StringBuilder result = new StringBuilder();

        boolean containsMce = containsMceAttributes(atts);
        
        Set<String> usedAttributes = new HashSet<String>();

        attributeLoop: for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            
            /**
             * mce_src, mce_href and mce_style sometimes find their way into the final
             * markup. We just want to remove the mce_ here. Sometimes there is both
             * style and mce_style, and this will fix that too.
             */
            if (containsMce) {
                for (String tag : MCE_TAGS) {
                    if (name.equals(tag) && containsAttribute(atts, "mce_"+tag)) {
                        continue attributeLoop;
                    } else if (name.equals(tag) && containsAttribute(atts, "_mce_"+tag)) {
                        continue attributeLoop;
                    } else if (name.equals(tag) && containsAttribute(atts, "data-mce-"+tag)) {
                        continue attributeLoop;
                    }
                    
                    if (name.equals("mce_" + tag) || name.equals("_mce_" + tag) || name.equals("data-mce-" + tag)) {
                        name = tag;
                    }
                }
            }
            
            if (!filter.isAttributeAllowed(tagName, name, value)) {
                continue;
            }

            String attrName = contentFilter.handleAttributeName(name, tagName);
            String attrValue = contentFilter.handleAttributeValue(htmlEscapeAll(value), tagName, name);

            // don't put in an attribute twice
            if (usedAttributes.add(attrName)) {
                result.append(" " + attrName + "=" + "\"" + attrValue + "\"");
            }
            
        }

        if (tagName.equals("img")) {
            if (!containsAttribute(atts, "border")) {
                result.append(" border=\"0\"");
            }
        }

        return result;
    }

    private boolean containsMceAttributes(final Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getLocalName(i).startsWith("mce_") || atts.getLocalName(i).startsWith("_mce_") || atts.getLocalName(i).startsWith("data-mce-")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAttribute(final Attributes atts, final String name) {
        for (int i = 0; i < atts.getLength(); i++) {
            if (atts.getLocalName(i).equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method does extra escaping to turn high-byte characters (back) into
     * HTML entities, as the Spring htmlEscape method doesn't see it necessary.
     */
    public String htmlEscapeAll(final String html) {
        String escaped = HtmlUtils.htmlEscape(html);
        StringBuffer result = new StringBuffer(escaped.length() * 2);
        for (int i = 0; i < escaped.length(); i++) {
            char character = escaped.charAt(i);
            if (character > MAX_ASCII_VALUE) {
                if (Character.isHighSurrogate(character)) {
                    int astralPoint = Character.toCodePoint(character, escaped.charAt(i+1));
                    result.append("&#" + astralPoint + ";");
                    i++;
                } else {
                    result.append("&#" + (int) character + ";");
                }
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

	public void setDelegate(HtmlContentWriter contentWriter) {
		throw new UnsupportedOperationException();
	}

}
