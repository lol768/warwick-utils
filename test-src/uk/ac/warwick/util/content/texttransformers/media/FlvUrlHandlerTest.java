package uk.ac.warwick.util.content.texttransformers.media;

import junit.framework.TestCase;

public class FlvUrlHandlerTest extends TestCase {
    public void testItRecognisesFlv() throws Exception {
        FlvMediaUrlHandler handler = new FlvMediaUrlHandler("", null);
        String url = "http://example.com/somewhere/video.FLV";
        assertTrue(handler.recognises(url));
    }
    
    public void testItRecognisesRtmpStreams() throws Exception {
        FlvMediaUrlHandler handler = new FlvMediaUrlHandler("", null);
        String url = "rmtp://example.com/mediaserver/id=1234?autoPlay=true";
        assertTrue(handler.recognises(url));
    }
}
