package uk.ac.warwick.util.content.texttransformers.media;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.JsoupHtmlParser;
import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.textile2.Textile2;
import uk.ac.warwick.util.content.textile2.TextileTextTransformer;

public class WmvMediaTagTest extends TestCase {
	
	public void testMediaTagHasOnClick(){
		String source = "[media]/services/its/intranet/projects/webdev/sandbox/mjones/intro.wmv[/media]";
		Textile2 t2 = new Textile2();
		String output = t2.apply(new MutableContent(new JsoupHtmlParser(), source)).getContent();
//		System.out.println(output);
		assertTrue(output.toLowerCase(), output.toLowerCase().indexOf("onclick") > 0);
		
	}

	@Override
	protected void setUp() throws Exception {
	    System.setProperty(
                TextileTextTransformer.TEXTILE_SERVICE_LOCATION_PROPERTY_KEY,
                "http://localhost:2000/textile");
        
        System.setProperty("textile.media.mp3WimpyPlayerLocation", "wimpy.swf");
        System.setProperty("textile.media.mp3AlternatePlayerLocation", "mp3player.swf");
        System.setProperty("textile.media.quicktimePreviewImage", "qt.png");
        System.setProperty("textile.media.windowsMediaPreviewImage", "wmp.jpg");
        System.setProperty("textile.media.flvPlayerLocation", "flyplayer.swf");
        System.setProperty("textile.latex.location", "/cgi-bin/mimetex.cgi");
	}
}
