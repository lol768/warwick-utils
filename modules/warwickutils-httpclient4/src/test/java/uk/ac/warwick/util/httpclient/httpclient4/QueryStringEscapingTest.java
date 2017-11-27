package uk.ac.warwick.util.httpclient.httpclient4;

import java.io.IOException;

import junit.framework.TestCase;

import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;

public final class QueryStringEscapingTest extends TestCase {
	
	private void retrieveUrl(String url) throws IOException {
		HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get, url);
		ex.setConnectionTimeout(500);
	
		Pair<Integer, String> status = ex.execute(HttpMethodExecutor.RESPONSE_AS_STRING);
		assertTrue("Should be able to access " + url + "(" + status.getLeft() + ")", status.getLeft() < 500);
	
		assertNotNull(status.getRight());
	}
	
	public void testEscapeAngleBrackets() throws Exception {
		String url = "http://www2.warwick.ac.uk/insite/?query=<value/>";
		
		retrieveUrl(url);
	}

	public void testWarwickTagReplacement() throws Exception {
		String url = "http://www2.warwick.ac.uk/insite/?dept=<warwick_deptcode/>";
		
		User user = new User();
		user.setDepartmentCode("IN");
		
		AbstractWarwickAwareHttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, url, ".warwick.ac.uk", user);
		ex.setSubstituteWarwickTags(true);
		
		Pair<Integer, String> status = ex.execute(HttpMethodExecutor.RESPONSE_AS_STRING);
		assertEquals(200, status.getLeft().intValue());
		
		assertEquals("https://warwick.ac.uk/insite/?dept=IN", ex.getRedirectUrl());
		
		assertNotNull(status.getRight());
	}
	
	public void testWarwickTagReplacementInPath() throws Exception {
		String url = "http://www2.warwick.ac.uk/<warwick_deptcode/>/";
		
		User user = new User();
		user.setDepartmentCode("insite");
		
		AbstractWarwickAwareHttpMethodExecutor ex = new WebServiceHttpMethodExecutor(Method.get, url, ".warwick.ac.uk", user);
		ex.setSubstituteWarwickTags(true);
		
		Pair<Integer, String> status = ex.execute(HttpMethodExecutor.RESPONSE_AS_STRING);
		assertEquals("Should be able to access " + url, 200, status.getLeft().intValue());
		
		assertEquals("https://warwick.ac.uk/insite/", ex.getRedirectUrl());
        
        assertNotNull(status.getRight());
	}

}
