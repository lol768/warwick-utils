package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import uk.ac.warwick.util.content.texttransformers.media.AudioMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MediaUrlTransformer;

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
	
	@Test
	public void withMediaTag() throws Exception {
		String input = "<html><head></head><body>Firstly [media]man.mp3[/media] and then [email]n.howes@warwick.ac.uk[/email] and that is it</body></html>";
		
		EmailTagTextTransformer email = new EmailTagTextTransformer();
		MediaUrlTransformer media = new MediaUrlTransformer(new HashMap<String, MediaUrlHandler>(){{ 
			put("audio", new AudioMediaUrlHandler("",""));
		}});
		
		String output = media.transform(input);
		output = email.transform(output);
		System.out.println(output);
	}

}
