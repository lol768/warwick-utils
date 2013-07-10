package uk.ac.warwick.util.content.texttransformers.embed;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.web.Uri;

public final class OEmbedTagTextTransformerTest {
    
    private final Mockery m = new JUnit4Mockery();

	private final OEmbed oembed = m.mock(OEmbed.class);
	
	private final OEmbedTagTextTransformer transformer = new OEmbedTagTextTransformer(oembed);

	@Test
	public void youtube() throws Exception {
	    final OEmbedResponse response = new OEmbedResponse();
	    response.setType("video");
	    response.setHtml("<video src='VIDEO'></video>");
	    
	    m.checking(new Expectations() {{
	        one(oembed).transformUrl(Uri.parse("http://www.youtube.com/watch?v=FfM_wS7qYfY")); will(returnValue(response));
	    }});
	    
		String input = "test words [embed]http://www.youtube.com/watch?v=FfM_wS7qYfY[/embed] test words";
        String output = transformer.apply(new MutableContent(null, input)).getContent();
        
        assertEquals("test words <video src='VIDEO'></video> test words", output);
	}

}
