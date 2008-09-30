package uk.ac.warwick.util.content.texttransformers.media;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.texttransformers.media.GoogleMediaUrlHandler;

public class GoogleMediaUrlHandlerTest extends TestCase {
    public void testItRecognisesGoogleDotCom() throws Exception {
        GoogleMediaUrlHandler handler = new GoogleMediaUrlHandler();
        String url = "http://video.google.com/videoplay?docId=-5851344091937046529";
        assertTrue(handler.recognises(url));
    }
    
    public void testItRecognisesGoogleDotCoDotuk() throws Exception {
        GoogleMediaUrlHandler handler = new GoogleMediaUrlHandler();
        String url = "http://video.google.co.uk/videoplay?docId=-5851344091937046529";
        assertTrue(handler.recognises(url));
    }
    
    public void testItRecognisesExtraParams() throws Exception {
        GoogleMediaUrlHandler handler = new GoogleMediaUrlHandler();
        String url = "http://video.google.co.uk/videoplay?param1=something&docId=-5851344091937046529&blah=hello";
        assertTrue(handler.recognises(url));
        assertEquals("http://video.google.com/googleplayer.swf?docId=-5851344091937046529", handler.getFlashUrl(url));
    }
}
