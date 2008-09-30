package uk.ac.warwick.util.content.texttransformers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AttributeStringParser {
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(?:(\\w+)\\s*=\\s*'([^']*)')" + //Attribute with single quotes 
            "|" +
            "(?:(\\w+)\\s*=\\s*\\&\\#8217\\;([^\\#]*)\\&\\#8217\\;)" + //Attribute with escaped single quotes 
            "|" +
            "(?:(\\w+)\\s*=\\s*\"([^\"]*)\")" + //Attribute with double quotes
            "|" +
            "(?:(\\w+)\\s*=\\s*\\&\\#8221\\;([^\\#]*)\\&\\#8221\\;)" + //Attribute with escaped double quotes
            "|" +
            "(?:(\\w+)\\s*=\\s*\\&quot\\;([^\\&]*)\\&quot\\;)" + //Attribute with escaped double quotes entity
            "|" +
            "(?:(\\w+)\\s*=\\s*([^\"'\\s]+)\\b)", //Attribute with NO quotes
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    
    private static final int NAME_GROUP_SINGLE = 1;
    private static final int VALUE_GROUP_SINGLE = 2;
    private static final int NAME_GROUP_SINGLE_ESCAPED = 3;
    private static final int VALUE_GROUP_SINGLE_ESCAPED = 4;
    private static final int NAME_GROUP_DOUBLE = 5;
    private static final int VALUE_GROUP_DOUBLE = 6;
    private static final int NAME_GROUP_DOUBLE_ESCAPED = 7;
    private static final int VALUE_GROUP_DOUBLE_ESCAPED = 8;
    private static final int NAME_GROUP_DOUBLE_ESCAPED_ENTITY = 9;
    private static final int VALUE_GROUP_DOUBLE_ESCAPED_ENTITY = 10;
    private static final int NAME_GROUP_NONE = 11;
    private static final int VALUE_GROUP_NONE = 12;
    
    private final List<Attribute> attributes;
    
    public AttributeStringParser(final String attributesString) {
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributesString.trim());

        attributes = new ArrayList<Attribute>();
        
        while (matcher.find()) {
            String name, value;
            if (matcher.group(NAME_GROUP_SINGLE) != null) {
                name = matcher.group(NAME_GROUP_SINGLE);
                value = matcher.group(VALUE_GROUP_SINGLE);
            } else if (matcher.group(NAME_GROUP_SINGLE_ESCAPED) != null) {
                name = matcher.group(NAME_GROUP_SINGLE_ESCAPED);
                value = matcher.group(VALUE_GROUP_SINGLE_ESCAPED);
            } else if (matcher.group(NAME_GROUP_DOUBLE) != null) { 
                name = matcher.group(NAME_GROUP_DOUBLE);
                value = matcher.group(VALUE_GROUP_DOUBLE);
            } else if (matcher.group(NAME_GROUP_DOUBLE_ESCAPED) != null) {
                name = matcher.group(NAME_GROUP_DOUBLE_ESCAPED);
                value = matcher.group(VALUE_GROUP_DOUBLE_ESCAPED);
            } else if (matcher.group(NAME_GROUP_DOUBLE_ESCAPED_ENTITY) != null) {
                name = matcher.group(NAME_GROUP_DOUBLE_ESCAPED_ENTITY);
                value = matcher.group(VALUE_GROUP_DOUBLE_ESCAPED_ENTITY);
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
