package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;

import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.content.MutableContent;

public class RelativeLinkTextTransformerTest {

    private RelativeLinkTextTransformer transformer;
    private String base;
    
    @Before
    public void setUp() throws Exception  {
        base = "http://www2.warwick.ac.uk/services/its/elab/";
        transformer = new RelativeLinkTextTransformer(base);
    }
    
    @Test
    public void relativeLinks() {
        assertEquals("Go to <a href=\""+base+"child\">The Next Page</a>.",
                transformer.apply(new MutableContent(null, "Go to <a href=\"child\">The Next Page</a>.")).getContent()
                );
      
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/services/its/\">The Next Page</a>.",
                transformer.apply(new MutableContent(null, "Go to <a href=\"../\">The Next Page</a>.")).getContent()
                );
    }
    
    @Test
    public void linksWithQueryStringOnly() {
        assertEquals("Go to <a href=\""+base+"?month=03\">Next month</a>.",
                transformer.apply(new MutableContent(null, "Go to <a href=\"?month=03\">Next month</a>.")).getContent()
                );
    }
    
    @Test
    public void absoluteLinks() {
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/happiness\">The Next Page</a>.",
                transformer.apply(new MutableContent(null, "Go to <a href=\"http://www2.warwick.ac.uk/happiness\">The Next Page</a>.")).getContent()
                );
    }
    
    @Test
    public void slashFirstLinks() {
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/craig/david\">The Next Page</a>.",
                transformer.apply(new MutableContent(null, "Go to <a href=\"/craig/david\">The Next Page</a>.")).getContent()
                );
        
        assertEquals("Go to <a href=\"http://www2.warwick.ac.uk/1/craig/david\">The Next Page</a>.",
                transformer.apply(new MutableContent(null, "Go to <a href=\"/1/craig/david\">The Next Page</a>.")).getContent()
                );
    }
    
    @Test
    public void aLotOfLinks() {
        assertEquals("<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>",
                transformer.apply(new MutableContent(null, 
                        "<a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>")).getContent()
                );
    }
    
    @Test
    public void dontMungeEmailTags() {
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
		
		assertEquals(input1, transformer.apply(new MutableContent(null, input1)).getContent());
		assertEquals(input2, transformer.apply(new MutableContent(null, input2)).getContent());
    }
    
    @Test
    public void rewriteScriptTags() {
    	String input1 = "<script type=\"text/javascript\">\nvar blah = '<a href=\"blah/blah\">';\n</script>";
    	String input2 = "<script type=\"text/javascript\" src=\"blah/blah\"></script>";
    	String input3 = "<script type=\"text/javascript\" src=\"blah/blah\">\nvar blah = '<a href=\"blah/blah\">';\n</script>";
    	
    	String expected1 = input1; // no change
    	String expected2 = "<script type=\"text/javascript\" src=\"http://www2.warwick.ac.uk/services/its/elab/blah/blah\"></script>"; // rewritten
    	String expected3 = "<script type=\"text/javascript\" src=\"http://www2.warwick.ac.uk/services/its/elab/blah/blah\">\nvar blah = '<a href=\"blah/blah\">';\n</script>"; // rewritten but not in content
    	
    	assertEquals(expected1, transformer.apply(new MutableContent(null, input1)).getContent());
    	assertEquals(expected2, transformer.apply(new MutableContent(null, input2)).getContent());
    	assertEquals(expected3, transformer.apply(new MutableContent(null, input3)).getContent());
    }

	@Test
	public void rewriteImageTagsUsingSrcset() {
		String input = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png 1x, ../../servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png 2x\" src=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";
		String expected = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png 1x, http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png 2x\" src=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";

		assertEquals(expected, transformer.apply(new MutableContent(null, input)).getContent());
	}

	@Test
	public void handleExtraSpacesWhenUsingSrcset() {
		String input = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png 1x , ../../servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png  2x \" src=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";
		String expected = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png 1x, http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png 2x\" src=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";

		assertEquals(expected, transformer.apply(new MutableContent(null, input)).getContent());
	}

	@Test
	public void noSizeOnFirstSrcsetElement() {
		String input = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png , ../../servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png  2x \" src=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";
		String expected = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png, http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png 2x\" src=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";

		assertEquals(expected, transformer.apply(new MutableContent(null, input)).getContent());
	}

	@Test
	public void rewriteImageTagsUsingSrcsetSingleQuotes() {
		String input1 = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset='../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png 1x, ../../servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png 2x' src=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";
		String expected1 = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" srcset='http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png 1x, http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress-2x.png 2x' src=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";

		assertEquals(expected1, transformer.apply(new MutableContent(null, input1)).getContent());
	}

	@Test
	public void imageWithNoSrcsetAtAll() {
		String input1 = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" src=\"../../servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";
		String expected1 = "<img border=\"0\" alt=\"Progress bar shows assignments which assignments are marked and ready to be marked\" src=\"http://www2.warwick.ac.uk/services/servicessupport/web/tabula/screenshots/cm2-markers-progress.png\" style=\"margin-top: 20px; margin-bottom: 20px; border: 1px solid black;\">";

		assertEquals(expected1, transformer.apply(new MutableContent(null, input1)).getContent());
	}

    /** SBTWO-3804 */
    @Test
    public void dontRewritePre() {
        assertEquals("<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>\n" +
                "<pre><a href=\"hello\">a</a>\n" +
                "<a href=\"hello\">a</a>\n</pre>" +
                "<a href=\"http://www2.warwick.ac.uk/services/its/elab/hello\">a</a>",
                transformer.apply(new MutableContent(null, 
                        "<a href=\"hello\">a</a>\n" +
                        "<pre><a href=\"hello\">a</a>\n" +
                        "<a href=\"hello\">a</a>\n</pre>" +
                        "<a href=\"hello\">a</a>")).getContent()
                );
    }
    
    @Test
    public void sbtwo4179() throws Exception {
        String input = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("sbtwo-4179-input.html")));
        String expected = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("sbtwo-4179-output.html")));
        
        String remoteUrl = "http://www2.warwick.ac.uk/services/its/servicessupport/training/course_cat/webpublishing_courses/";

        RelativeLinkTextTransformer linkTransformer = new RelativeLinkTextTransformer(remoteUrl);
        assertEquals(expected, linkTransformer.apply(new MutableContent(null, input)).getContent());
    }
    
    @Test
    public void sbtwo4182() throws Exception {
        String input = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("sbtwo-4182-input.html")));
        String expected = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("sbtwo-4182-output.html")));
        
        String remoteUrl = "http://search.warwick.ac.uk/exampapers/plain?q=ec104";

        RelativeLinkTextTransformer linkTransformer = new RelativeLinkTextTransformer(remoteUrl);
        assertEquals(expected, linkTransformer.apply(new MutableContent(null, input)).getContent());
    }
}
