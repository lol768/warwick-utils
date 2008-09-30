package uk.ac.warwick.util.content.cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * A map-backed implementation of Attributes
 * 
 * @author Mat Mannion
 */
public final class MapBackedAttributes implements Attributes {
    
    private final List<NameValuePair> attrs;
    
    public MapBackedAttributes(final Map<String, String> attributes) {
        attrs = new ArrayList<NameValuePair>();
        for (String key : attributes.keySet()) {
            attrs.add(new NameValuePair(key, attributes.get(key)));
        }
    }
    
    public int getLength() {
        return attrs.size();
    }
    
    public String getLocalName(int index) {
        return attrs.get(index).name;
    }
    
    public String getValue(int index) {
        return attrs.get(index).value;
    }

    public int getIndex(String arg0) {
        throw new UnsupportedOperationException();
    }

    public int getIndex(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    public String getQName(int arg0) {
        throw new UnsupportedOperationException();
    }

    public String getType(int arg0) {
        throw new UnsupportedOperationException();
    }

    public String getType(String arg0) {
        throw new UnsupportedOperationException();
    }

    public String getType(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    public String getURI(int arg0) {
        throw new UnsupportedOperationException();
    }

    public String getValue(String arg0) {
        throw new UnsupportedOperationException();
    }

    public String getValue(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }
    
    private static class NameValuePair {
        
        private final String name;
        private final String value;
        
        public NameValuePair(String theName, String theValue) {
            this.name = theName;
            this.value = theValue;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
        
    }

}
