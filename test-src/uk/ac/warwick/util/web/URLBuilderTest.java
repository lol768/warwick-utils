package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

public class URLBuilderTest {

	@Test public void replacingQuery() throws Exception {
		URL url = new URL("http://www.example.com/fac/soc?view=daily");
		URLBuilder builder = new URLBuilder(url);
		builder.setQuery(new URLQuery(url.getQuery())
			.add("page", "/fog")
			.add("escapedText","http://x")
			.toQueryString());
		URL newUrl = builder.toURL();
		assertEquals("http://www.example.com/fac/soc?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
	}
	
	@Test public void replacingQueryWithTrailingSlash() throws Exception {
        URL url = new URL("http://www.example.com/fac/soc/?view=daily");
        URLBuilder builder = new URLBuilder(url);
        builder.setQuery(new URLQuery(url.getQuery())
            .add("page", "/fog")
            .add("escapedText","http://x")
            .toQueryString());
        URL newUrl = builder.toURL();
        assertEquals("http://www.example.com/fac/soc/?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
    }
    
    @Test public void replacingQueryWithNoPath() throws Exception {
        URL url = new URL("http://www.example.com?view=daily");
        URLBuilder builder = new URLBuilder(url);
        builder.setQuery(new URLQuery(url.getQuery())
            .add("page", "/fog")
            .add("escapedText","http://x")
            .toQueryString());
        URL newUrl = builder.toURL();
        assertEquals("http://www.example.com?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
    }
	
	@Test public void replacingQueryWithNoPathWithSlash() throws Exception {
        URL url = new URL("http://www.example.com/?view=daily");
        URLBuilder builder = new URLBuilder(url);
        builder.setQuery(new URLQuery(url.getQuery())
            .add("page", "/fog")
            .add("escapedText","http://x")
            .toQueryString());
        URL newUrl = builder.toURL();
        assertEquals("http://www.example.com/?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
    }

}
