package uk.ac.warwick.util.content.texttransformers.media;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

public final class FlvMediaUrlHandlerTest {
	
	private final Mockery m = new JUnit4Mockery();

	private final FlvMediaUrlHandler handler = new FlvMediaUrlHandler("playerLocation", "newPlayerLocation");
	
	private final MetadataHandler metadataHandler = m.mock(MetadataHandler.class);

	@Test
	public void recognises() {
		assertTrue(handler.recognises("file.flv"));
		assertTrue(handler.recognises("file.f4v"));
		assertTrue(handler.recognises("file.f4p"));
		assertTrue(handler.recognises("file.mp4"));
		assertTrue(handler.recognises("file.m4v"));
		assertTrue(handler.recognises("rtmp://something.something"));
		assertFalse(handler.recognises("anything"));
	}

	@Test
	public void itWorks() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("random", new java.util.Random());
		parameters.put("align", "left");
		
		assertTrue(handler.getHtml("file.mp4", parameters).startsWith("<notextile>"));
		assertTrue(handler.getHtml("file.mp4", parameters).contains("\"425\",\"370\""));
	}
	
	@Test
	public void bigVideo() {
		handler.setMetadataHandler(metadataHandler);
		
		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("random", new java.util.Random());
		parameters.put("align", "left");
		
		m.checking(new Expectations() {{
			one(metadataHandler).handle("file.mp4", parameters); will(new Action() {
				public void describeTo(Description description) {
					description.appendText("populate metadata");
				}
				public Object invoke(Invocation invocation) throws Throwable {
					parameters.put("width", "1,024");
					parameters.put("height", "768");
					return null;
				}
				
			});
		}});
		
		String html = handler.getHtml("file.mp4", parameters);
		
		System.out.println(html);
		
		assertTrue(html.startsWith("<notextile>"));
		assertTrue(html.contains("\"1024\",\"788\""));
		
		m.assertIsSatisfied();
	}

}
