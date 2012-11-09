package uk.ac.warwick.util.web.bind;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public final class UrlPropertyEditorTest extends TestCase {
    public void testSetAsTextWithValidUrl() throws MalformedURLException {
        String url = "http://google.co.uk";
        // sanity check
        URL expectedUrl = new URL(url);

        UrlPropertyEditor editor = new UrlPropertyEditor();
        editor.setAsText(url);
        assertEquals(expectedUrl, editor.getValue());
    }

    public void testSetAsTextWithEmptyUrl() throws MalformedURLException {
        String url = "";

        UrlPropertyEditor editor = new UrlPropertyEditor();
        editor.setAsText(url);
        assertNull(editor.getValue());
    }

    public void testSetAsTextWithUnknownUrl() throws MalformedURLException {
        String url = "this shouldn't work";

        UrlPropertyEditor editor = new UrlPropertyEditor();
        try {
            editor.setAsText(url);
            fail("expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            //fine
        }
    }
    

    public void testGetAsText() throws MalformedURLException {
        String url = "http://google.co.uk";

        UrlPropertyEditor editor = new UrlPropertyEditor();

        editor.setValue(new URL(url));
        assertEquals("url", url, editor.getAsText());
    }

    public void testGetAsTextWithNullContent() {
        UrlPropertyEditor editor = new UrlPropertyEditor();

        editor.setValue(null);
        assertNull(editor.getAsText());
    }
}
