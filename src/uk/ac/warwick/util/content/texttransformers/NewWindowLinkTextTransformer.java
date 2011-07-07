package uk.ac.warwick.util.content.texttransformers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.util.content.MutableContent;

/**
 * Transformer which detects links having target="_blank" and modifies the link
 * so it can be styled and differentiated from regular links.
 * 
 * @author cusebr
 */
public final class NewWindowLinkTextTransformer implements TextTransformer {

    // public static final String CSS_CLASS = "newWindow";
    public static final String HTML_IMAGE = "<img class='targetBlank' alt='' title='Link opens in a new window' src='/static_war/images/shim.gif' />";

    // private static final String HTML_DOUBLE_QUOTE = "&quot;";

    public MutableContent apply(final MutableContent mc) {
        return rewriteLinks(mc);
    }

    MutableContent rewriteLinks(final MutableContent mc) {
       TagTransformer linkTransformer = new TagTransformer("a", new TextPatternTransformer.Callback() {
           public String transform(final String input, final MutableContent mc) {
               return rewriteLink(input);
           }
       }, false);
       return linkTransformer.apply(mc);
    }

    /**
     * @param String
     *            elementHtml The whole of the element to be rewritten,
     *            including the contents and the closing tag.
     */
    private String rewriteLink(final String elementHtml) {
        Matcher matcher = Pattern.compile("^<a (.+)?>(.+)</a>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL).matcher(elementHtml);
        if (matcher.find()) {
            String attributesString = matcher.group(1);
            AttributeStringParser parser = new AttributeStringParser(attributesString);

            StringBuffer result = new StringBuffer();
            result.append(elementHtml.substring(0, matcher.end(2)));
            if (hasNewWindowAttribute(parser.getAttributes())) {
                result.append(HTML_IMAGE);
            }
            result.append(elementHtml.substring(matcher.end(2)));
            return result.toString();
        } else {
            return elementHtml;
        }
    }

    private boolean hasNewWindowAttribute(final List<Attribute> attributes) {
        for (Attribute a: attributes) {
            if (a.getName().equalsIgnoreCase("target")
                    && (a.getValue().equalsIgnoreCase("_blank") || a.getValue().equalsIgnoreCase("_new"))) {
                return true;
            }
        }
        return false;
    }

}
