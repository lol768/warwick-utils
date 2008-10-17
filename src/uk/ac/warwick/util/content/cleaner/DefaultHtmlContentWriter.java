package uk.ac.warwick.util.content.cleaner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;

import uk.ac.warwick.util.core.HtmlUtils;

/**
 * Default implementation
 * 
 * @author Mat Mannion
 */
public final class DefaultHtmlContentWriter implements HtmlContentWriter {

    public static final String[] MCE_TAGS = new String[] {"href","src","style"};
    
    private static final int MAX_ASCII_VALUE = 127;

    private final TagAndAttributeFilter filter;

    private final BodyContentFilter contentFilter;

    private final Set<String> selfClosers = new HashSet<String>(Arrays.asList(new String[] { "img", "br", "hr", "input", "area" }));

    public DefaultHtmlContentWriter(TagAndAttributeFilter tagAndAttributeFilter, BodyContentFilter cFilter) {
        this.filter = tagAndAttributeFilter;
        this.contentFilter = cFilter;
    }

    public String renderStartTag(String tagName, Attributes atts) {
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
        return selfClosers.contains(tagName);
    }

    /**
     * Build a string of escape name="value" pairs from an Attributes object.
     */
    private StringBuilder constructAttributes(final Attributes atts, final String tagName) {
        StringBuilder result = new StringBuilder();

        boolean containsMce = containsMceAttributes(atts);
        

        attributeLoop: for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getLocalName(i);
            String value = atts.getValue(i);
            if (!filter.isAttributeAllowed(tagName, name, value)) {
                continue;
            }
            

            /**
             * mce_src, mce_href and mce_style sometimes find their way into the final
             * markup. We just want to remove the mce_ here. Sometimes there is both
             * style and mce_style, and this will fix that too.
             */
            if (containsMce) {
                for (String tag : MCE_TAGS) {
                    if (name.equals(tag) && containsAttribute(atts, "mce_"+tag)) {
                        continue attributeLoop;
                    }
                    if (name.equals("mce_" + tag)) {
                        name = tag;
                    }
                }
            }

            String attrName = contentFilter.handleAttributeName(name, tagName);
            String attrValue = contentFilter.handleAttributeValue(htmlEscapeAll(value), tagName, name);

            result.append(" " + attrName + "=" + "\"" + attrValue + "\"");
            
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
            if (atts.getLocalName(i).startsWith("mce_")) {
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
                result.append("&#" + (int) character + ";");
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
