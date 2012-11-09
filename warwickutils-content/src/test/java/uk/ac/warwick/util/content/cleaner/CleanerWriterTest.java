package uk.ac.warwick.util.content.cleaner;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import uk.ac.warwick.util.content.MutableContent;


public class CleanerWriterTest {
	private Mockery mockery = new Mockery();
	
	// UTL-63 A comment "<!-->" causes comment of negative length. Check that we ignore it.
	@Test public void tinyComment() throws Exception {
		final TagAndAttributeFilter filter = mockery.mock(TagAndAttributeFilter.class);
		mockery.checking(new Expectations(){{
			allowing(filter); will(returnValue(true));
		}});
		CleanerWriter cw = new CleanerWriter(filter, new MutableContent(null, null));
		cw.characters("Hello".toCharArray(), 0, 5);
		cw.comment("".toCharArray(), 0, -2);
		cw.characters("Hello".toCharArray(), 0, 4);
		
		assertEquals("HelloHell", cw.getOutput());
	}
}
