package uk.ac.warwick.util.content.texttransformers.media;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.Maps;

public final class FlvMediaUrlHandlerTest {
	
	private final Mockery m = new JUnit4Mockery();

	private final FlvMediaUrlHandler handler = new FlvMediaUrlHandler(
	        "http://www2.warwick.ac.uk/static_war/render/flvplayer.swf", 
	        "http://www2.warwick.ac.uk/static_war/render/flvplayer-4.2.swf");
	
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
		
		assertTrue(handler.getHtml("file.mp4", parameters, null).startsWith("<notextile>"));
		assertTrue(handler.getHtml("file.mp4", parameters, null).contains("requestedWidth = 425"));
		assertTrue(handler.getHtml("file.mp4", parameters, null).contains("requestedHeight = 350"));
	}
	
	@Test
	public void bigVideo() {
		handler.setMetadataHandler(metadataHandler);
		
		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("random", new java.util.Random());
		parameters.put("align", "left");
		
		m.checking(new Expectations() {{
			one(metadataHandler).handle("../big_buck_bunny.mp4", parameters, null); will(new Action() {
				public void describeTo(Description description) {
					description.appendText("populate metadata");
				}
				public Object invoke(Invocation invocation) throws Throwable {
					parameters.put("width", "6,40");
					parameters.put("height", "360");
					parameters.put("previewimage", "http://augustus.warwick.ac.uk/services/its/elab/about/people/mmannion/big_buck_bunny.mp4?preview");
					parameters.put("mime_type", "video/mp4");
					return null;
				}
				
			});
		}});
		
		String html = handler.getHtml("../big_buck_bunny.mp4", parameters, null);
		
		assertTrue(html.startsWith("<notextile>"));
		assertTrue(html.contains("requestedWidth = 640"));
		
		m.assertIsSatisfied();
	}
	
	@Test
	public void withPercentageWidthAndAutoHeight() throws Exception {
	    Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("random", new java.util.Random());
        parameters.put("align", "left");
        parameters.put("width", "100%");
        parameters.put("height", "auto");
        
        String html = handler.getHtml("file.mp4", parameters, null);
        
        assertTrue(html.startsWith("<notextile>"));
		assertTrue(handler.getHtml("file.mp4", parameters, null).contains("requestedWidth = \"100%\""));
        
        assertTrue(html.contains("preload=\"none\""));
        parameters.put("preload", "metadata");
        html = handler.getHtml("file.mp4", parameters, null);
        assertTrue(html.contains("vidEl.setAttribute('preload', 'metadata');"));
	}
	
	@Test
	public void withTitle() throws Exception {
	    Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("random", new java.util.Random());
        parameters.put("align", "left");
        parameters.put("title", "Click here for a good time");
        
        String html = handler.getHtml("file.mp4", parameters, null);
        
        assertTrue(html.startsWith("<notextile>"));
        assertTrue(html.contains("<a href=\"file.mp4?forceOpenSave=true\""));
        assertTrue(html.contains("Click here for a good time</a>"));
        assertTrue(html.contains("<head>"));
	}
	
	@Test
	public void withAlternateRenditions() throws Exception {
	    handler.setMetadataHandler(metadataHandler);
        
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("random", new java.util.Random());
        parameters.put("align", "left");
        
        m.checking(new Expectations() {{
            one(metadataHandler).handle("file.mp4", parameters, null); will(new Action() {
                public void describeTo(Description description) {
                    description.appendText("populate metadata");
                }
                public Object invoke(Invocation invocation) throws Throwable {
                    parameters.put("width", "6,40");
                    parameters.put("height", "360");
                    parameters.put("previewimage", "file.jpg");
                    parameters.put("mime_type", "video/mp4");
                    
                    Map<String, String> alternateRenditions = Maps.newLinkedHashMap();
                    alternateRenditions.put("video/webm", "file.webm");
                    alternateRenditions.put("video/ogg", "file.ogg");
                    
                    parameters.put("alternateRenditions", alternateRenditions);                    
                    
                    return null;
                }
                
            });
        }});
        
        String html = handler.getHtml("file.mp4", parameters, null);
        assertFalse(html.contains("vidEl.insert({top:new Element('source', {"));
        assertTrue(html.startsWith("<notextile>"));
		assertTrue(html.contains("requestedWidth = 640"));
		assertTrue(html.contains("requestedHeight = 360"));
        
        m.assertIsSatisfied();
	}
	
	@Test
    public void withAlternateRenditionsMP4NotFirst() throws Exception {
	    handler.setMetadataHandler(metadataHandler);
        
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("random", new java.util.Random());
        parameters.put("align", "left");
        
        m.checking(new Expectations() {{
            one(metadataHandler).handle("file.webm", parameters, null); will(new Action() {
                public void describeTo(Description description) {
                    description.appendText("populate metadata");
                }
                public Object invoke(Invocation invocation) throws Throwable {
                    parameters.put("width", "6,40");
                    parameters.put("height", "360");
                    parameters.put("previewimage", "file.jpg");
                    parameters.put("mime_type", "video/webm");
                    
                    Map<String, String> alternateRenditions = Maps.newLinkedHashMap();
                    alternateRenditions.put("video/mp4", "file.mp4");
                    alternateRenditions.put("video/ogg", "file.ogg");
                    
                    parameters.put("alternateRenditions", alternateRenditions);                    
                    
                    return null;
                }
                
            });
        }});
        
        String html = handler.getHtml("file.webm", parameters, null);
        
        System.out.println(html);
        
        assertTrue(html.startsWith("<notextile>"));
        assertTrue(html.indexOf("source src=\"file.mp4\"") < html.indexOf("source src=\"file.ogg\""));
		assertTrue(html.contains("requestedWidth = 640"));
		assertTrue(html.contains("requestedHeight = 360"));
        
        m.assertIsSatisfied();
    }

}
