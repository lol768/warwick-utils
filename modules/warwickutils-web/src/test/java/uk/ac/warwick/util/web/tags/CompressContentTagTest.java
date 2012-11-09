package uk.ac.warwick.util.web.tags;

import junit.framework.TestCase;

import org.springframework.mock.web.MockBodyContent;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;

public class CompressContentTagTest extends TestCase {
	/**
	 * Check we don't compress all the spaces out of ourselves
	 */
	public void testExcessiveCompression() throws Exception {
		String input = "<input\n"+ 
			"                           type=\"hidden\"\n"+ 
			"                           name=\"editor\"\n"+ 
			"                           value=\"editMceContent\"\n"+
			"                            />";
		String expected = "<input type=\"hidden\" name=\"editor\" value=\"editMceContent\" />";
		
		CompressContentTag tag = new CompressContentTag();
		assertEquals(expected, doTag(tag, input));
	}
	
	public void testRemoveInterTagSpaces() throws Exception {
		String input = "<input type=\"hidden\" name=\"a\" />\n" +
				"   <input type=\"hidden\" name=\"b\" />";
		String withSpace = "<input type=\"hidden\" name=\"a\" /> <input type=\"hidden\" name=\"b\" />";
		String withoutSpace = "<input type=\"hidden\" name=\"a\" /><input type=\"hidden\" name=\"b\" />";
		
		// check we haven't affected default behaviour first
		CompressContentTag tag = new CompressContentTag();
		assertEquals(withSpace, doTag(tag, input));
		
		// then engage galactic tag space destroyer mark I		
		CompressContentTag tag2 = new CompressContentTag();
		tag2.setRemoveInterTagSpaces(true);
		assertEquals(withoutSpace, doTag(tag2, input));
	}
	
	private String doTag(CompressContentTag tag, String body) throws Exception  {
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		MockBodyContent content = new MockBodyContent(body, mockResponse);
		MockPageContext context = new MockPageContext(null,null,mockResponse);
		tag.setBodyContent(content);
		tag.setPageContext(context);
		tag.doEndTag();
		return mockResponse.getContentAsString();
	}
}
