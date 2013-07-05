package uk.ac.warwick.util.content.texttransformers.embed;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.util.content.MutableContent;

public final class OEmbedTagTextTransformerTest {

	// FIXME at the moment this test requires an internet connection
	private final OEmbed oembed = new OEmbed();
	
	private final OEmbedTagTextTransformer transformer = new OEmbedTagTextTransformer(oembed);
	
	@Before public void setupProviders() {
		oembed.setAutodiscovery(true);
	}

	@Test
	public void youtube() {
		String input = "test words [embed]http://www.youtube.com/watch?v=FfM_wS7qYfY[/embed] test words";
        String output = transformer.apply(new MutableContent(null, input)).getContent();
        
        assertTrue(output.contains("<iframe"));
	}

}
