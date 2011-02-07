package uk.ac.warwick.util.content.texttransformers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.media.AudioMediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MediaUrlHandler;
import uk.ac.warwick.util.content.texttransformers.media.MediaUrlTransformer;

public final class EmailTagTextTransformerTest {
	
	@Test
	public void itWorks() throws Exception {
		String input = "[email]m.mannion@warwick.ac.uk[/email]";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		assertTrue(output.contains("<span id"));
	}
	
	@Test
	public void noScriptForHtmlComments() throws Exception {
		String input = "<!-- Here is some other [email]mat.mannion@gmail.com[/email] stuff --> <p>[email]m.mannion@warwick.ac.uk[/email]</p><!-- Here is some other [email]mat.mannion@gmail.com[/email] stuff --> <!-- Here is some other [email]mat.mannion@gmail.com[/email] stuff -->";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		// Only one instance of this.
		assertEquals("Should only have one email address wrapped in HTML", output.indexOf("<span id"), output.lastIndexOf("<span id"));
	}
	
	@Test
	public void retainsExistingScripts() throws Exception {
		String input = "<html><head><script src=\"blah\"></script></head><body>[email]m.mannion@warwick.ac.uk[/email]</body></html>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		assertTrue(output.contains("<span id"));
	}
	
	@Test
	public void multipleEmails() throws Exception {
		String input = "<p>[email]m.mannion@warwick.ac.uk[/email]</p>\n<p>[email]n.howes@warwick.ac.uk[/email]</p>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		assertTrue(output.contains("<span id"));
	}
	
	@Test
	public void doesntChangeIfNoTag() throws Exception {
		String input = "<p>Here is just a normal paragraph</p>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		String expected = input; // no change
		
		assertEquals(expected, output);
	}
	
	@Test
	public void doesntChangeNoTextile() throws Exception {
		String input = "<notextile>[email]m.mannion@warwick.ac.uk[/email]</notextile>";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		String expected = input; // no change
		
		assertEquals(expected, output);
	}
	
	@Test
	public void differentAddress() throws Exception {
		String input = "[email address=m.mannion@warwick.ac.uk]Email me[/email]";
		
		EmailTagTextTransformer transformer = new EmailTagTextTransformer();
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		assertTrue(output.contains("<span id"));
	}
	
	@SuppressWarnings("serial")
	@Test
	public void playsNicelyWithMediaTags() throws Exception {
		String input = "<html><body><p>[media]blah.mp3[/media]</p><p>[email]mat.mannion@gmail.com[/email]</p></body></html>";
		
		CompositeTextTransformer transformer = new CompositeTextTransformer(Arrays.asList(
				new MediaUrlTransformer(new HashMap<String, MediaUrlHandler>() {{
					put("mp3", new AudioMediaUrlHandler("wimpy", "opo"));
				}}, null),
				new EmailTagTextTransformer()
		));
		String output = transformer.apply(new MutableContent(null, input)).getContent();
		
		// the main problem is that it has more than one <body> tag because it adds the html stuff in twice. laaaaame
		Pattern p = Pattern.compile("<body>");
		Matcher m = p.matcher(output);
		
		int matches = 0;
		while (m.find()) {
			matches++;
		}
		
		assertEquals(1, matches);
	}

}
