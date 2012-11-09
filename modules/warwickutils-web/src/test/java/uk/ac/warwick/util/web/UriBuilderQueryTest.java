package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

public class UriBuilderQueryTest {
    
    private static final Uri URI = Uri.parse("http://www.warwick.ac.uk");

	@Test public void parsing() throws Exception {
		String string = "one+one=1&two=2&target=http%3A%2F%2Fwww2.warwick.ac.uk%2Fpage%3Fescaped%3Dyes";
		
		UriBuilder builder = new UriBuilder(URI).setQuery(string);
		assertEquals("1", builder.getQueryParameter("one one"));
		assertEquals("2", builder.getQueryParameter("two"));
		assertEquals("http://www2.warwick.ac.uk/page?escaped=yes", builder.getQueryParameter("target"));
	}
	
	@Test public void toMap() throws Exception {
	    String string = "one+one=1&two=2&target=http%3A%2F%2Fwww2.warwick.ac.uk%2Fpage%3Fescaped%3Dyes";
	    UriBuilder builder = new UriBuilder(URI).setQuery(string);
        
        Map<String, List<String>> map = builder.getQueryParameters();
        assertEquals(3, map.size());
        assertEquals(Lists.newArrayList("1"), map.get("one one"));
        assertEquals(Lists.newArrayList("2"), map.get("two"));
        assertEquals(Lists.newArrayList("http://www2.warwick.ac.uk/page?escaped=yes"), map.get("target"));
	}
	
	@Test public void emptyValue() throws Exception {
	    UriBuilder builder = new UriBuilder(URI).setQuery("one=1&two=");
		assertEquals("1", builder.getQueryParameter("one"));
		assertEquals("", builder.getQueryParameter("two"));
		assertNull(builder.getQueryParameter("three"));
	}
	
	@Test public void totallyEmpty() throws Exception {
	    UriBuilder builder = new UriBuilder(URI).setQuery("");
		assertEquals("", builder.getQuery());
		builder.setQuery(null);
		assertNull(builder.getQuery());
	}
	
	@Test(expected=IllegalArgumentException.class) public void invalidEncoding() throws Exception {
	    new UriBuilder(URI).setQuery("value=50%");
	}
	
	@Test public void generating() throws Exception {
	    UriBuilder builder = new UriBuilder(URI).setQuery("old=yes").addQueryParameter("new", "yes").addQueryParameter("strength", "50%").addQueryParameter("url", "http://example.com/");
		assertEquals("old=yes&new=yes&strength=50%25&url=http%3A%2F%2Fexample.com%2F", builder.getQuery());
	}
	
	@Test public void stripping() throws Exception {
	    UriBuilder builder = new UriBuilder(URI).setQuery("view=daily&sbrPage=%2Ffac%2Fsoc%2Fbin&index=3&unwanted=go+away");
	    builder.removeQueryParameter("sbrPage").removeQueryParameter("unwanted");
		assertEquals("view=daily&index=3", builder.getQuery());
		assertNull(builder.getQueryParameter("sbrPage"));
	}

}
