package uk.ac.warwick.util.content.texttransformers;

import junit.framework.TestCase;

public class AttributeStringParserTest extends TestCase {
    public void testSingleQuotes() {
        String input = " param-1='value 1'  param2='value 2' ";
        AttributeStringParser parser = new AttributeStringParser(input);
        assertEquals("value 1", parser.getValue("param-1"));
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
    
    public void testNoQuotesNbsp() {
        String input = " param1=value1&nbsp; param2=value2 ";
        AttributeStringParser parser = new AttributeStringParser(input);
        //List<Attribute> attrs = parser.getAttributes();
        assertEquals("value1", parser.getValue("param1"));
        assertEquals("value2", parser.getValue("param2"));
    }
    
    public void testEscapedQuotes() {
    	String input = "param1=&#8217;value1&#8217; param2=&#8221;value2&#8221; param3=&quot;value3&quot;";
    	AttributeStringParser parser = new AttributeStringParser(input);
	   assertEquals("value1", parser.getValue("param1"));
       assertEquals("value2", parser.getValue("param2"));
       assertEquals("value3", parser.getValue("param3"));
    }
    
    public void testBackslashEscaping() {
    	String input = "allowed2=\"The \\\"Magic\\\" tag\" allowed1='It\\'s escaped!'";
    	AttributeStringParser parser = new AttributeStringParser(input);
    	assertEquals("It's escaped!", parser.getValue("allowed1"));
    	assertEquals("The \"Magic\" tag", parser.getValue("allowed2"));
    }
    
    // Backslashes only work on the quotes you are using for quoting
    public void testBackslashesForDifferentQuotes() {
    	String input = "allowed2=\"Bob\\'s Cobs\"";
    	AttributeStringParser parser = new AttributeStringParser(input);
    	// still has the backslash in it, because we used double quotes.
    	assertEquals("Bob\\'s Cobs", parser.getValue("allowed2"));
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
