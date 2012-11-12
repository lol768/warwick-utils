package uk.ac.warwick.util.httpclient;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.userlookup.User;
import uk.ac.warwick.util.web.Uri;

public class WarwickTagUrlManglerTest {

    @Test
	public void departmentCodeTag() {
		WarwickTagUrlMangler mangler = new WarwickTagUrlMangler();
		String testUrl = "http://www2.warwick.ac.uk/foo/<warwick_deptcode/>/bar";
		User u = new User();
		assertEquals("http://www2.warwick.ac.uk/foo//bar", mangler.substituteWarwickTags(Uri.parse(testUrl), u).toString());	
		u.setDepartmentCode("IN");
		assertEquals("http://www2.warwick.ac.uk/foo/IN/bar", mangler.substituteWarwickTags(Uri.parse(testUrl), u).toString());	
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void everythingEver() {
		WarwickTagUrlMangler mangler = new WarwickTagUrlMangler();
		User u = new User();
		u.setFirstName("Fred");
		u.setLastName("Test");
		u.setDepartmentCode("IN");
		u.setUserId("AUserId");
		u.setEmail("somewhere@something");
		u.setToken("token_foo");
		u.setWarwickId("123WarwickID");
		
		String testUrl="http://www.warwick.ac.uk/?yes=<warwick_username/>|<warwick_userid/>|<warwick_useremail/>|<warwick_token/>|<warwick_idnumber/>|<warwick_deptcode/>";
		String result = "http://www.warwick.ac.uk/?yes=Fred+Test%7CAUserId%7Csomewhere%40something%7Ctoken_foo%7C123WarwickID%7CIN";
		assertEquals(result, mangler.substituteWarwickTags(Uri.parse(testUrl), u).toString());
	}
	
	@Test
	public void plainTextReplacement() {
	    User u = new User();
	    
	    WarwickTagUrlMangler mangler = new WarwickTagUrlMangler();
	    assertEquals("don't change my spaces to %20", mangler.substituteWarwickTags("don't change my spaces to %20", u));
	    assertEquals("here is a ", mangler.substituteWarwickTags("here is a <warwick_deptcode/>", u));
	    
	    u.setDepartmentCode("IN");
	    assertEquals("here is a IN", mangler.substituteWarwickTags("here is a <warwick_deptcode/>", u));
	}
	
}