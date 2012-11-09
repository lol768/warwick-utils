package uk.ac.warwick.util.content.texttransformers.media;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AttributeStringParser {
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(?:(\\w+)\\s*=\\s*'([^']*)')" + //Attribute with single quotes 
            "|" +
            "(?:(\\w+)\\s*=\\s*\"([^\"]*)\")" + //Attribute with double quotes
            "|" +
            "(?:(\\w+)\\s*=\\s*([^\"'\\s]+)\\b)", //Attribute with NO quotes
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    
    private static final int NAME_GROUP_SINGLE = 1;
    private static final int VALUE_GROUP_SINGLE = 2;
    private static final int NAME_GROUP_DOUBLE = 3;
    private static final int VALUE_GROUP_DOUBLE = 4;
    private static final int NAME_GROUP_NONE = 5;
    private static final int VALUE_GROUP_NONE = 6;
    
    private final List<Attribute> attributes;
    
    public AttributeStringParser(final String attributesString) {
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributesString);

        attributes = new ArrayList<Attribute>();
        
        while (matcher.find()) {
            String name, value;
            if (matcher.group(NAME_GROUP_SINGLE) != null) {
                name = matcher.group(NAME_GROUP_SINGLE);
                value = matcher.group(VALUE_GROUP_SINGLE);
            } else if (matcher.group(NAME_GROUP_DOUBLE) != null) { 
                name = matcher.group(NAME_GROUP_DOUBLE);
                value = matcher.group(VALUE_GROUP_DOUBLE);
            } else {
                name = matcher.group(NAME_GROUP_NONE);
                value = matcher.group(VALUE_GROUP_NONE);
            }
            attributes.add(new Attribute(name, value));
        }
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
