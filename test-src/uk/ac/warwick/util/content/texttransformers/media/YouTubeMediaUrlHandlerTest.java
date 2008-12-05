package uk.ac.warwick.util.content.texttransformers.media;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class YouTubeMediaUrlHandlerTest extends TestCase {
    public void testBasicUrl() throws MalformedURLException {
        String videoId = "abcdef";
        String address = "http://www.YouTube.com/watch?v=" + videoId;
        checkAddressWorks(videoId, address);
    }
    
    public void testUrlWithExtraParams() throws MalformedURLException {
        String videoId = "TFl-iBlW14Y";
        String address = "http://www.youtube.com/watch?param=hello&v="+videoId+"&mode=related&search=";
        checkAddressWorks(videoId, address);
    }
    
    public void testAnotherWithExtraParams() throws MalformedURLException {
        String videoId = "P83FGtPCuvc";
        String address = "http://youtube.com/watch?v="+videoId+"&search=daily%20show%20internet%20tubes";
        checkAddressWorks(videoId, address);
    }
    
    
    public void testUrlWithoutCorrectParam() throws Exception {
        String videoId = "TFl-iBlW14Y";
        String address = "http://www.youtube.com/watch?param=hello&someotherparam="+videoId+"&mode=related&search=";
        checkAddressDoesntWork(videoId, address);
    }
    
    //  just checking that the URL query is returned as-is.
    public void testUrlQueryIsAsExpected() throws Exception {
        String params = "param1=value1&param2=value+number+2";
        URL url = new URL("http://www.domain.com/address?"+params);
        assertEquals(params, url.getQuery());
    }
    
    public void testInternationalisation() throws Exception {
        String videoId = "bbP2_y-rgLE";
        String address = "http://uk.youtube.com/watch?v=" + videoId;
        checkAddressWorks(videoId, address);
    }
    
    private void checkAddressWorks(String videoId, String address) throws MalformedURLException {
        String url = address;
        YouTubeMediaUrlHandler handler = new YouTubeMediaUrlHandler();
        assertTrue("should recognise url", handler.recognises(url));
        assertEquals("http://www.youtube.com/v/"+videoId + "&fs=1&hl=en", handler.getFlashUrl(url));
    }
    
    private void checkAddressDoesntWork(String videoId, String address) throws MalformedURLException {
        String url = address;
        YouTubeMediaUrlHandler handler = new YouTubeMediaUrlHandler();
        assertFalse("should NOT recognise url", handler.recognises(url));
        try {
            handler.getFlashUrl(url);
        } catch (IllegalArgumentException e) {
            //fine.
            return;
        }
        fail("Should have thrown an exception at the inappropriate URL");
    }
}
