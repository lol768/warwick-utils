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

}
