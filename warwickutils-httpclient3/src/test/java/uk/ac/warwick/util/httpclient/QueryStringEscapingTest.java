package uk.ac.warwick.util.httpclient;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.ConnectTimeoutException;

import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor.Method;
import uk.ac.warwick.util.web.Uri;

public final class QueryStringEscapingTest extends TestCase {
	
	private void retrieveUrl(String url) throws IOException {
		try {
			HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
			ex.setUrl(Uri.parse(url));
			ex.setConnectionTimeout(500);
		
			int status = ex.execute();
			assertTrue("Should be able to access " + url + "(" + status + ")", status < 500);
		
			ex.retrieveContentsAsString();
		
			ex.close();
		} catch (ConnectTimeoutException e) {
			//we're okay with this
		}
	}
	
	public void testEscapeAngleBrackets() throws Exception {
		String url = "http://www2.warwick.ac.uk/insite/?query=<value/>";
		
		retrieveUrl(url);
	}

	public void testWarwickTagReplacement() throws Exception {
		String url = "http://www2.warwick.ac.uk/insite/?dept=<warwick_deptcode/>";
		
		User user = new User();
		user.setDepartmentCode("IN");
		
		AbstractWarwickAwareHttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, user, ".warwick.ac.uk");
		ex.setUrl(Uri.parse(url));
		ex.setSubstituteWarwickTags(true);
		
		int status = ex.execute();
		assertEquals(200, status);
		
		assertEquals("http://www2.warwick.ac.uk/insite/?dept=IN", ex.getMethod().getURI().toString());
		
		ex.retrieveContentsAsString();
		
		ex.close();
	}
	
	public void testWarwickTagReplacementInPath() throws Exception {
		String url = "http://www2.warwick.ac.uk/<warwick_deptcode/>/";
		
		User user = new User();
		user.setDepartmentCode("insite");
		
		AbstractWarwickAwareHttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, user, ".warwick.ac.uk");
		ex.setUrl(Uri.parse(url));
		ex.setSubstituteWarwickTags(true);
		
		int status = ex.execute();
		assertEquals("Should be able to access " + url, 200, status);
		
		assertEquals("http://www2.warwick.ac.uk/insite/", ex.getMethod().getURI().toString());
		
		ex.retrieveContentsAsString();
		
		ex.close();
	}

}
