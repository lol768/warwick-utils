package uk.ac.warwick.util.web.filter.stack;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FilterMappingParserTest {
    
    FilterMappingParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new FilterMappingParserImpl();
    }
    
    @Test public void root() {
        assertMatches("/", "/");
        assertDoesNotMatch("/", "/path", "/render/file.css");
    }
    
    @Test public void prefixes() {
        assertMatches("/render/*",
                "/render/renderPage.htm", "/render/", "/render/api/yes");
        assertDoesNotMatch("/render/*",
                "/hello", "/Render",
                "/context/render/", "/context/render/renderPage.htm");
    }
    
    @Test public void missingSlash() {
        assertMatches("/file/*", "/file", "/file/", "/file/3");
        assertDoesNotMatch("/file/*", "/files");
    }
    
    @Test public void extensions() {
        assertMatches("*.css",
                "/render/page.css", "/page.css", "/whatever/blah.do.css");
        assertDoesNotMatch("*.css",
                "/render/css", "/edit/page.css/more");
    }

    @Test public void wildcardExtension() {
        assertMatches("/render/renderPage.*",
                "/render/renderPage.htm", "/render/renderPage.html");
        assertDoesNotMatch("/render/renderPage.*",
                "/render/renderPage/anything", "/hello", "/Render",
                "/context/render/", "/context/render/renderPage.htm");
    }
    
    @Test public void exact() {
        assertMatches("/edit/api/deleteWebsite","/edit/api/deleteWebsite");
        assertDoesNotMatch("/edit/api/deleteWebsite",
                "/edit/api/deleteWebsites", "/do/edit/api/deleteWebsite");
    }
    
    private void assertMatches(String mapping, String... urls) {
        for (String url : urls) {
            assertTrue(parser.matches(url, mapping));
        }
    }
    
    private void assertDoesNotMatch(String mapping, String... urls) {
        for (String url : urls) {
            assertFalse(parser.matches(url, mapping));
        }
    }

}
