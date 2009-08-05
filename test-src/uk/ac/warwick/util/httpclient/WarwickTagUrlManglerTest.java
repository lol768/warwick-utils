package uk.ac.warwick.util.httpclient;

import junit.framework.TestCase;
import uk.ac.warwick.userlookup.User;


public class WarwickTagUrlManglerTest extends TestCase{

	public void testDepartmentCodeTag() {
		WarwickTagUrlMangler mangler = new WarwickTagUrlMangler();
		String testUrl = "http://www2.warwick.ac.uk/foo/<warwick_deptcode/>/bar";
		User u = new User();
		assertEquals("http://www2.warwick.ac.uk/foo//bar", mangler.substituteWarwickTags(testUrl, u));	
		u.setDepartmentCode("IN");
		assertEquals("http://www2.warwick.ac.uk/foo/IN/bar", mangler.substituteWarwickTags(testUrl, u));	
	}
	
	@SuppressWarnings("deprecation")
	public void testEverythingEver() {
		WarwickTagUrlMangler mangler = new WarwickTagUrlMangler();
		User u = new User();
		u.setFirstName("Fred");
		u.setLastName("Test");
		u.setDepartmentCode("IN");
		u.setUserId("AUserId");
		u.setEmail("somewhere@something");
		u.setToken("token_foo");
		u.setWarwickId("123WarwickID");
		
		String testUrl="<warwick_username/>|<warwick_userid/>|<warwick_useremail/>|<warwick_token/>|<warwick_idnumber/>|<warwick_deptcode/>";
		String result = "Fred+Test|AUserId|somewhere%40something|token_foo|123WarwickID|IN";
		assertEquals(result, mangler.substituteWarwickTags(testUrl, u));
		
	}
	
}
