package uk.ac.warwick.util.content.texttransformers;

import junit.framework.TestCase;

public class AttributeStringParserTest extends TestCase {
    public void testSingleQuotes() {
        String input = " param1='value 1'  param2='value 2' ";
        AttributeStringParser parser = new AttributeStringParser(input);
        assertEquals("value 1", parser.getValue("param1"));
        assertEquals("value 2", parser.getValue("param2"));
    }
    
    public void testDoubleQuotes() {
        String input = " param1=\"value 1\"  param2=\"value 2\" ";
        AttributeStringParser parser = new AttributeStringParser(input);
        assertEquals("value 1", parser.getValue("param1"));
        assertEquals("value 2", parser.getValue("param2"));
    }
    
    public void testNoQuotes() {
        String input = " param1=value1  param2=value2 ";
        AttributeStringParser parser = new AttributeStringParser(input);
        //List<Attribute> attrs = parser.getAttributes();
        assertEquals("value1", parser.getValue("param1"));
        assertEquals("value2", parser.getValue("param2"));
    }
    
    /**
     * Test that double quotes don't have to be escaped when inside
     * a single-quoted value.
     */
    public void testDoubleQuotesAllowedInSingleQuotes() {
        String input = " param1='Say \"Hello\"'  param2=value2 ";
        AttributeStringParser parser = new AttributeStringParser(input);
        assertEquals("Say \"Hello\"", parser.getValue("param1"));
        assertEquals("value2", parser.getValue("param2"));
    }
    
    /**
     * Test that single quotes don't have to be escaped when inside
     * a double-quoted value.
     */
    public void testSingleQuotesAllowedInDoubleQuotes() {
        String input = " param1=\"Bill's\"  param2=value2 ";
        AttributeStringParser parser = new AttributeStringParser(input);
        assertEquals("Bill's", parser.getValue("param1"));
        assertEquals("value2", parser.getValue("param2"));
    }
}
