package uk.ac.warwick.util.content.textile2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public final class TextileStringTest {
	
	@Before
	public void setUp() throws Exception {
        System.setProperty("textile.media.mp3WimpyPlayerLocation", "wimpy.swf");
        System.setProperty("textile.media.mp3AlternatePlayerLocation", "mp3player.swf");
        System.setProperty("textile.media.quicktimePreviewImage", "qt.png");
        System.setProperty("textile.media.windowsMediaPreviewImage", "wmp.jpg");
        System.setProperty("textile.media.flvPlayerLocation", "flyplayer.swf");
        System.setProperty("textile.latex.location", "/cgi-bin/mimetex.cgi");
	}

	@Test
	public void noTrailingSpaceInLinks() throws Exception {
		String input = "Here is the first line\n\nHere is the second line, http://www.warwick.ac.uk. Whoopie!\n\nHere is the third line";
		String output = convertForums(input);
		
		String expected = "<p>Here is the first line</p><p>Here is the second line, <a rel=\"nofollow\" href=\"http://www.warwick.ac.uk\">http://www.warwick.ac.uk</a>. Whoopie!</p><p>Here is the third line</p>";
		
		assertEquals(expected, output.replace("\n",""));
	}
	
	public String convertForums(String input) {
		TextileString textileString = new TextileString(input);
		
		textileString.setCorrectHtml(true);
		// textileString.setOnlyAllowTags("ul,ol,li,p,strong,em,b,i,a,code,pre,script");
		textileString.setDisallowTags("script,style,link,blink,object,applet,canvas");
		textileString.setAddNoFollow(true);
		textileString.setAllowJavascriptHandlers(false);

		String formattedText = textileString.getHtml();
		if (formattedText == null) {
		    throwError("Error formatting text, null value returned for: "+input);
		}
		return formattedText;
	}

	private void throwError(String string) {
		fail(string);
	}

}
