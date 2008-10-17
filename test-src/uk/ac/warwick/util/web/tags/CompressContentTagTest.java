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
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();
		MockBodyContent content = new MockBodyContent(input, mockResponse);
		MockPageContext context = new MockPageContext(null,null,mockResponse);
		tag.setBodyContent(content);
		tag.setPageContext(context);
		
		tag.doEndTag();
		
		String result =  mockResponse.getContentAsString();
		assertEquals(expected, result);
	}
}
