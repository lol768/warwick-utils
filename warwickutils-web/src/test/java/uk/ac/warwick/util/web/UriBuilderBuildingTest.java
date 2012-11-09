package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import org.junit.Test;

public class UriBuilderBuildingTest {

	@Test public void replacingQuery() throws Exception {
		Uri url = Uri.parse("http://www.example.com/fac/soc?view=daily");
		UriBuilder builder = new UriBuilder(url);
		builder.addQueryParameter("page", "/fog").addQueryParameter("escapedText","http://x");
		Uri newUrl = builder.toUri();
		assertEquals("http://www.example.com/fac/soc?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
	}
	
	@Test public void replacingQueryWithTrailingSlash() throws Exception {
	    Uri url = Uri.parse("http://www.example.com/fac/soc/?view=daily");
        UriBuilder builder = new UriBuilder(url);
        builder.addQueryParameter("page", "/fog").addQueryParameter("escapedText","http://x");
        Uri newUrl = builder.toUri();
        assertEquals("http://www.example.com/fac/soc/?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
    }
    
    @Test public void replacingQueryWithNoPath() throws Exception {
        Uri url = Uri.parse("http://www.example.com?view=daily");
        UriBuilder builder = new UriBuilder(url);
        builder.addQueryParameter("page", "/fog").addQueryParameter("escapedText","http://x");
        Uri newUrl = builder.toUri();
        assertEquals("http://www.example.com?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
    }
	
	@Test public void replacingQueryWithNoPathWithSlash() throws Exception {
	    Uri url = Uri.parse("http://www.example.com/?view=daily");
        UriBuilder builder = new UriBuilder(url);
        builder.addQueryParameter("page", "/fog").addQueryParameter("escapedText","http://x");
        Uri newUrl = builder.toUri();
        assertEquals("http://www.example.com/?view=daily&page=%2Ffog&escapedText=http%3A%2F%2Fx", newUrl.toString());
    }

}
