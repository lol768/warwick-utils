package uk.ac.warwick.util.content.texttransformers.media;

public final class Attribute {
    private String name;
    private String value;
    
    Attribute(final String theName, final String theValue) {
        name = theName;
        value = theValue;
    }
    
    public String getName() { return name; }
    public String getValue() { return value; }
    
    public void setName(final String newName) { name = newName; }
    public void setValue(final String newValue) { value = newValue; }
}