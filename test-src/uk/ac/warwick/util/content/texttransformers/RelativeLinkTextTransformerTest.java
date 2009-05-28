package uk.ac.warwick.util.content.texttransformers;

import junit.framework.TestCase;

public class RelativeLinkTextTransformerTest extends TestCase {

    private RelativeLinkTextTransformer transformer;
    private String base;
    
    public void setUp() throws Exception  {
        base = "http://www2.warwick.ac.uk/services/its/elab/";
        transformer = new RelativeLinkTextTransformer(base);
    }
    
    public void testRelativeLinks() {
        assertEquals("Go to <a href=\""+base+"child\">The Next Page</a>.",
                transformer.transform("Go to <a href=\"child\">The Next Page</a>.")
                );
      
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/services/its/\">The Next Page</a>.",
                transformer.transform("Go to <a href=\"../\">The Next Page</a>.")
                );
    }
    
    public void testLinksWithQueryStringOnly() {
        assertEquals("Go to <a href=\""+base+"?month=03\">Next month</a>.",
                transformer.transform("Go to <a href=\"?month=03\">Next month</a>.")
                );
    }
    
    public void testAbsoluteLinks() {
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/happiness\">The Next Page</a>.",
                transformer.transform("Go to <a href=\"http://www2.warwick.ac.uk/happiness\">The Next Page</a>.")
                );
    }
    
    public void testSlashFirstLinks() {
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/craig/david\">The Next Page</a>.",
                transformer.transform("Go to <a href=\"/craig/david\">The Next Page</a>.")
                );
        
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/1/craig/david\">The Next Page</a>.",
                transformer.transform("Go to <a href=\"/1/craig/david\">The Next Page</a>.")
                );
    }
    
    public void testALotOfLinks() {
        assertEquals("<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>",
                transformer.transform("<a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>")
                );
    }
    
    public void testDontMungeEmailTags() {
    	String input1 = "<script type=\"text/javascript\">\n";
		input1 += "Event.onDOMReady(function() { var email302088326 = '<a href';\n";
		input1 += "email302088326 += '=\"mai';\n";
		input1 += "email302088326 += 'lto:&';\n";
		input1 += "email302088326 += '#x6d;&';\n";
		input1 += "email302088326 += '#46;&#109;&#x';\n";
		input1 += "email302088326 += '61;&#110;&#110;&#x';\n";
		input1 += "email302088326 += '69;&';\n";
		input1 += "email302088326 += '#111;';\n";
		input1 += "email302088326 += '&#x6e;&#x40;&#x77;';\n";
		input1 += "email302088326 += '&#97;&#';\n";
		input1 += "email302088326 += '1';\n";
		input1 += "email302088326 += '14;&';\n";
		input1 += "email302088326 += '#x77;&#10';\n";
		input1 += "email302088326 += '5;&#x63;';\n";
		input1 += "email302088326 += '&#x6b';\n";
		input1 += "email302088326 += ';&';\n";
		input1 += "email302088326 += '#46;&#97;&#99;&#';\n";
		input1 += "email302088326 += '46;&#1';\n";
		input1 += "email302088326 += '17';\n";
		input1 += "email302088326 += ';&#x6b;\">&#x6d;&#x2e;&#x';\n";
		input1 += "email302088326 += '6d;&#97;&#x6e';\n";
		input1 += "email302088326 += ';&#x';\n";
		input1 += "email302088326 += '6e;';\n";
		input1 += "email302088326 += '&#105;&#111;&#x';\n";
		input1 += "email302088326 += '6e';\n";
		input1 += "email302088326 += ';&';\n";
		input1 += "email302088326 += '#x40;&#119;&#97;&#x72;&#x77;&#105;&#99;&#x6b;&#46;&#97;';\n";
		input1 += "email302088326 += '&#99';\n";
		input1 += "email302088326 += ';&#x2';\n";
		input1 += "email302088326 += 'e;&#117;&#107;</a>';\n";
		input1 += "Element.update('email302088326',email302088326);\n";
		input1 += "});\n";
		input1 += "</script>";
    	
    	String input2 = "<script type=\"text/javascript\">\n";
		input2 += "Event.onDOMReady(function() { var email302088326 = '<a href=\"mailto:&';\n";
		input2 += "email302088326 += '#x6d;&';\n";
		input2 += "email302088326 += '#46;&#109;&#x';\n";
		input2 += "email302088326 += '61;&#110;&#110;&#x';\n";
		input2 += "email302088326 += '69;&';\n";
		input2 += "email302088326 += '#111;';\n";
		input2 += "email302088326 += '&#x6e;&#x40;&#x77;';\n";
		input2 += "email302088326 += '&#97;&#';\n";
		input2 += "email302088326 += '1';\n";
		input2 += "email302088326 += '14;&';\n";
		input2 += "email302088326 += '#x77;&#10';\n";
		input2 += "email302088326 += '5;&#x63;';\n";
		input2 += "email302088326 += '&#x6b';\n";
		input2 += "email302088326 += ';&';\n";
		input2 += "email302088326 += '#46;&#97;&#99;&#';\n";
		input2 += "email302088326 += '46;&#1';\n";
		input2 += "email302088326 += '17';\n";
		input2 += "email302088326 += ';&#x6b;\">&#x6d;&#x2e;&#x';\n";
		input2 += "email302088326 += '6d;&#97;&#x6e';\n";
		input2 += "email302088326 += ';&#x';\n";
		input2 += "email302088326 += '6e;';\n";
		input2 += "email302088326 += '&#105;&#111;&#x';\n";
		input2 += "email302088326 += '6e';\n";
		input2 += "email302088326 += ';&';\n";
		input2 += "email302088326 += '#x40;&#119;&#97;&#x72;&#x77;&#105;&#99;&#x6b;&#46;&#97;';\n";
		input2 += "email302088326 += '&#99';\n";
		input2 += "email302088326 += ';&#x2';\n";
		input2 += "email302088326 += 'e;&#117;&#107;</a>';\n";
		input2 += "Element.update('email302088326',email302088326);\n";
		input2 += "});\n";
		input2 += "</script>";
		
		assertEquals(input1, transformer.transform(input1));
		assertEquals(input2, transformer.transform(input2));
    }
    
    public void testRewriteScriptTags() {
    	String input1 = "<script type=\"text/javascript\">\nvar blah = '<a href=\"blah/blah\">';\n</script>";
    	String input2 = "<script type=\"text/javascript\" src=\"blah/blah\"></script>";
    	String input3 = "<script type=\"text/javascript\" src=\"blah/blah\">\nvar blah = '<a href=\"blah/blah\">';\n</script>";
    	
    	String expected1 = input1; // no change
    	String expected2 = "<script type=\"text/javascript\" src=\"http://www2.warwick.ac.uk/services/its/elab/blah/blah\"></script>"; // rewritten
    	String expected3 = "<script type=\"text/javascript\" src=\"http://www2.warwick.ac.uk/services/its/elab/blah/blah\">\nvar blah = '<a href=\"blah/blah\">';\n</script>"; // rewritten but not in content
    	
    	assertEquals(expected1, transformer.transform(input1));
    	assertEquals(expected2, transformer.transform(input2));
    	assertEquals(expected3, transformer.transform(input3));
    }

}
