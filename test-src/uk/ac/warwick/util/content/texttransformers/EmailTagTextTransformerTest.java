package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;
import org.junit.Test;

public final class EmailTagTextTransformerTest {
	
	@Test
	public void itWorks() throws Exception {
		String input = "[email]m.mannion@warwick.ac.uk[/email]";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.transform(input);
		
		assertTrue(output.contains("<span id"));
	}
	
	@Test
	public void retainsExistingScripts() throws Exception {
		String input = "<html><head><script src=\"blah\"></script></head><body>[email]m.mannion@warwick.ac.uk[/email]</body></html>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.transform(input);
		
		assertTrue(output.contains("<span id"));
	}
	
	@Test
	public void multipleEmails() throws Exception {
		String input = "<p>[email]m.mannion@warwick.ac.uk[/email]</p>\n<p>[email]n.howes@warwick.ac.uk[/email]</p>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.transform(input);
		
		assertTrue(output.contains("<span id"));
	}
	
	@Test
	public void doesntChangeIfNoTag() throws Exception {
		String input = "<p>Here is just a normal paragraph</p>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.transform(input);
		
		String expected = input; // no change
		
		assertEquals(expected, output);
	}
	
	@Test
	public void doesntChangeNoTextile() throws Exception {
		String input = "<notextile>[email]m.mannion@warwick.ac.uk[/email]</notextile>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.transform(input);
		
		String expected = input; // no change
		
		assertEquals(expected, output);
	}
	
	@Test
	public void differentAddress() throws Exception {
		String input = "[email address=m.mannion@warwick.ac.uk]Email me[/email]";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.transform(input);
		
		assertTrue(output.contains("<span id"));
	}

}
