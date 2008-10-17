package uk.ac.warwick.util.content.texttransformers.media;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.textile2.Textile2;

public class WmvMediaTagTest extends TestCase {
	
	public void testMediaTagHasOnClick(){
		String source = "[media]/services/its/intranet/projects/webdev/sandbox/mjones/intro.wmv[/media]";
		Textile2 t2 = new Textile2();
		String output = t2.process(source);
//		System.out.println(output);
		assertTrue(output.toLowerCase(), output.toLowerCase().indexOf("onclick") > 0);
		
	}

	@Override
	protected void setUp() throws Exception {
		System.setProperty("textile.media.mp3WimpyPlayerLocation", "");
		System.setProperty("textile.media.mp3AlternatePlayerLocation", "");
		System.setProperty("textile.media.quicktimePreviewImage", "");
		System.setProperty("textile.media.windowsMediaPreviewImage", "");
		System.setProperty("textile.media.flvPlayerLocation", "");
		System.setProperty("textile.latex.location", "/cgi-bin/mimetex.cgi");
	}
}
