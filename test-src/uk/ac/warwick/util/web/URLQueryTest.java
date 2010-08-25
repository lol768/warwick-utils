package uk.ac.warwick.util.web;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

public class URLQueryTest {

	@Test public void parsing() throws Exception {
		String string = "one+one=1&two=2&target=http%3A%2F%2Fwww2.warwick.ac.uk%2Fpage%3Fescaped%3Dyes";
		URLQuery query = new URLQuery(string);
		assertEquals("1", query.getFirst("one one"));
		assertEquals("2", query.getFirst("two"));
		assertEquals("http://www2.warwick.ac.uk/page?escaped=yes", query.getFirst("target"));
	}
	
	@Test public void toMap() throws Exception {
	    String string = "one+one=1&two=2&target=http%3A%2F%2Fwww2.warwick.ac.uk%2Fpage%3Fescaped%3Dyes";
        URLQuery query = new URLQuery(string);
        
        Map<String, String> map = query.toMap();
        assertEquals(3, map.size());
        assertEquals("1", map.get("one one"));
        assertEquals("2", map.get("two"));
        assertEquals("http://www2.warwick.ac.uk/page?escaped=yes", map.get("target"));
	}
	
	@Test public void emptyValue() throws Exception {
		URLQuery query = new URLQuery("one=1&two=");
		assertEquals("1", query.getFirst("one"));
		assertEquals("", query.getFirst("two"));
		assertNull(query.getFirst("three"));
	}
	
	@Test public void totallyEmpty() throws Exception {
		URLQuery query = new URLQuery("");
		assertEquals("", query.toQueryString());
		query = new URLQuery(null);
		assertEquals("", query.toQueryString());
	}
	
	@Test(expected=IllegalArgumentException.class) public void invalidEncoding() throws Exception {
	    new URLQuery("value=50%");
	}
	
	@Test public void generating() throws Exception {
		URLQuery query = new URLQuery("old=yes").add("new", "yes").add("strength", "50%").add("url", "http://example.com/");
		assertEquals("old=yes&new=yes&strength=50%25&url=http%3A%2F%2Fexample.com%2F", query.toQueryString());
	}
	
	@Test public void stripping() throws Exception {
		URLQuery query = new URLQuery("view=daily&sbrPage=%2Ffac%2Fsoc%2Fbin&index=3&unwanted=go+away");
		query.removeKeys("sbrPage", "unwanted");
		assertEquals("view=daily&index=3", query.toQueryString());
		assertNull(query.getFirst("sbrPage"));
	}

}
