package uk.ac.warwick.util.content.texttransformers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transformer which detects links having target="_blank" and modifies the link
 * so it can be styled and differentiated from regular links.
 * 
 * @author cusebr
 */
public final class NewWindowLinkTextTransformer implements TextTransformer {

    //public static final String CSS_CLASS = "newWindow";
    public static final String HTML_IMAGE = "<img class='targetBlank' alt='' title='Link opens in a new window' src='/static_war/images/shim.gif' />";
    
//    private static final String HTML_DOUBLE_QUOTE = "&quot;";

    public String transform(final String html) {
        return rewriteLinks(html);
    }

    String rewriteLinks(final String html) {
       TagTransformer linkTransformer = new TagTransformer("a");
       return linkTransformer.transform(html, new TextPatternTransformer.Callback() {
            public String transform(final String input) {
                return rewriteLink(input);
            }    
       });
    }

//    private String rewriteLink(final String tagHtml) {
//        AttributeStringParser parser = new AttributeStringParser(tagHtml);
//        List<Attribute> attributes = parser.getAttributes();
//        
//        if (hasNewWindowAttribute(attributes)) {
//            addOrAppendCSSClass(attributes);
//        } else {
//            removeCSSClassIfNecessary(attributes);
//        }
//        
//        //reconstruct the link from scratch.
//        StringBuilder sb = new StringBuilder();
//        sb.append("<a");
//        for (Attribute attribute : attributes) {
//            sb.append(" " + attribute.getName() + "=");
//            sb.append("\"" + attribute.getValue().replace("\"", HTML_DOUBLE_QUOTE)  + "\"");
//        }
//        sb.append(">");
//        
//        return sb.toString();
//    }
    
    /**
     * @param String elementHtml The whole of the element to be rewritten,
     * including the contents and the closing tag.
     */
    private String rewriteLink(final String elementHtml) {
        Matcher matcher = Pattern.compile("^<a (.+)?>(.+)</a>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL).matcher(elementHtml);
        if (matcher.find()) {
            String attributesString = matcher.group(1);
            AttributeStringParser parser = new AttributeStringParser(attributesString);
            
            StringBuffer result = new StringBuffer();
            result.append(elementHtml.substring(0,matcher.end(2)));
            if (hasNewWindowAttribute(parser.getAttributes())) {
                result.append(HTML_IMAGE);
            }
            result.append(elementHtml.substring(matcher.end(2)));
            return result.toString();
        } else {
            return elementHtml;
        }
    }
    

//    private void removeCSSClassIfNecessary(final List<Attribute> attributes) {
//        for (Attribute a : attributes) {
//            if ( a.getName().equalsIgnoreCase("class") ) {
//                List<String> classes = new ArrayList<String>();                
//                
//                String originalValue = a.getValue();
//                StringTokenizer tokenizer = new StringTokenizer(originalValue);
//                while (tokenizer.hasMoreTokens()) {
//                    String cssClass = tokenizer.nextToken();
//                    if (!cssClass.equals(CSS_CLASS)) {
//                        classes.add(cssClass);
//                    }
//                }
//                
//                a.setValue(StringUtils.collectionToDelimitedString(classes, " "));
//            }
//        }
//    }

//    private void addOrAppendCSSClass(final List<Attribute> attributes) {
//        for (Attribute a : attributes) {
//            if ( a.getName().equalsIgnoreCase("class") ) {
//                String originalValue = a.getValue();
//                if (!originalValue.contains(CSS_CLASS)) {
//                    a.setValue(originalValue + " " + CSS_CLASS);  
//                }
//                //At this point we either already had the class or just added it. We're done
//                return;
//            }
//        }
//        //No class attribute found, make a new one
//        attributes.add(new Attribute("class", CSS_CLASS));
//    }

    private boolean hasNewWindowAttribute(final List<Attribute> attributes) {
        for (Attribute a : attributes) {
            if ( a.getName().equalsIgnoreCase("target") && 
                    ( a.getValue().equalsIgnoreCase("_blank") ||
                      a.getValue().equalsIgnoreCase("_new")) ) {
                return true;
            }
        }
        return false;
    }

}
