package uk.ac.warwick.util.content.cleaner;

import java.util.Map;

public interface BodyContentFilter {
    
    BodyContentFilter DO_NOTHING_CONTENT_FILTER = new BodyContentFilter() {

        public String handleAttributeName(String input, String tagName) {
            return input;
        }

        public String handleAttributeValue(String input, String tagName, String attributeName) {
            return input;
        }

        public String handleCharacters(String input) {
            return input;
        }

        public String handleTagString(String input, String tagName, Map<String, String> attributes) {
            return input;
        }
        
    };
    
    String handleCharacters(String input);
    
    String handleAttributeName(String input, String tagName);
    
    String handleAttributeValue(String input, String tagName, String attributeName);
    
    String handleTagString(String input, String tagName, Map<String, String> attributes);

}
