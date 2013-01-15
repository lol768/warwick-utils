package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for Uri.
 */
public class UriTest {
    @Test
    public void parseFull() {
        Uri uri = Uri.parse("http://apache.org/foo?a=b&a=c&b=d+e#blah");

        assertEquals("http", uri.getScheme());
        assertEquals("apache.org", uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertEquals("a=b&a=c&b=d+e", uri.getQuery());
        Collection<String> params = Arrays.asList("b", "c");
        assertEquals(params, uri.getQueryParameters("a"));
        assertEquals("b", uri.getQueryParameter("a"));
        assertEquals("d e", uri.getQueryParameter("b"));
        assertEquals("blah", uri.getFragment());

        assertEquals("http://apache.org/foo?a=b&a=c&b=d+e#blah", uri.toString());
    }

    @Test
    public void parseNoScheme() {
        Uri uri = Uri.parse("//apache.org/foo?a=b&a=c&b=d+e#blah");

        assertNull(uri.getScheme());
        assertEquals("apache.org", uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertEquals("a=b&a=c&b=d+e", uri.getQuery());
        Collection<String> params = Arrays.asList("b", "c");
        assertEquals(params, uri.getQueryParameters("a"));
        assertEquals("b", uri.getQueryParameter("a"));
        assertEquals("d e", uri.getQueryParameter("b"));
        assertEquals("blah", uri.getFragment());
    }

    @Test
    public void parseNoAuthority() {
        Uri uri = Uri.parse("http:/foo?a=b&a=c&b=d+e#blah");

        assertEquals("http", uri.getScheme());
        assertNull(uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertEquals("a=b&a=c&b=d+e", uri.getQuery());
        Collection<String> params = Arrays.asList("b", "c");
        assertEquals(params, uri.getQueryParameters("a"));
        assertEquals("b", uri.getQueryParameter("a"));
        assertEquals("d e", uri.getQueryParameter("b"));
        assertEquals("blah", uri.getFragment());
    }

    @Test
    public void parseNoPath() {
        Uri uri = Uri.parse("http://apache.org?a=b&a=c&b=d+e#blah");

        assertEquals("http", uri.getScheme());
        assertEquals("apache.org", uri.getAuthority());
        // Path is never null.
        assertEquals("", uri.getPath());
        assertEquals("a=b&a=c&b=d+e", uri.getQuery());
        Collection<String> params = Arrays.asList("b", "c");
        assertEquals(params, uri.getQueryParameters("a"));
        assertEquals("b", uri.getQueryParameter("a"));
        assertEquals("d e", uri.getQueryParameter("b"));
        assertEquals("blah", uri.getFragment());
    }

    @Test
    public void parseNoQuery() {
        Uri uri = Uri.parse("http://apache.org/foo#blah");

        assertEquals("http", uri.getScheme());
        assertEquals("apache.org", uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertNull(uri.getQuery());
        assertEquals(0, uri.getQueryParameters().size());
        assertNull(uri.getQueryParameter("foo"));
        assertEquals("blah", uri.getFragment());
    }

    @Test
    public void parseNoFragment() {
        Uri uri = Uri.parse("http://apache.org/foo?a=b&a=c&b=d+e");

        assertEquals("http", uri.getScheme());
        assertEquals("apache.org", uri.getAuthority());
        assertEquals("/foo", uri.getPath());
        assertEquals("a=b&a=c&b=d+e", uri.getQuery());
        assertNull(uri.getFragment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidHost() {
        Uri.parse("http://A&E%#%#%/foo?a=b#blah");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidScheme() {
        Uri.parse("----://apache.org/foo?a=b#blah");
    }

    @Test
    public void parseInvalidPath() {
    	// As of TAB-334 this is now fine.
    	Uri uri = Uri.parse("http://apache.org/foo\\---(&%?a=b#blah");
    	
    	assertEquals("http", uri.getScheme());
        assertEquals("apache.org", uri.getAuthority());
        assertEquals("/foo%5C---(&%25", uri.getPath());
        assertEquals("a=b", uri.getQuery());
        assertEquals("blah", uri.getFragment());
    }

    @Test
    public void toJavaUri() {
        URI javaUri = URI.create("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri uri = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");

        assertEquals(javaUri, uri.toJavaUri());
    }

    @Test
    public void toJavaUriWithSpecialChars() {
        URI javaUri = URI.create("http://example.org/foo/bar/baz?blah=bl%25ah#boo");
        Uri uri = Uri.parse("http://example.org/foo/bar/baz?blah=bl%25ah#boo");

        assertEquals(javaUri, uri.toJavaUri());
    }

    @Test
    public void fromJavaUri() throws Exception {
        URI javaUri = URI.create("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri uri = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");

        assertEquals(uri, Uri.fromJavaUri(javaUri));
    }

    @Test
    public void resolveFragment() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("#bar");

        assertEquals("http://example.org/foo/bar/baz?blah=blah#bar", base.resolve(other).toString());
    }

    @Test
    public void resolveQuery() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("?hello=world");

        assertEquals("http://example.org/foo/bar/?hello=world", base.resolve(other).toString());
    }

    @Test
    public void resolvePathIncludesSubdirs() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("fez/../huey/./dewey/../louis");

        assertEquals("http://example.org/foo/bar/huey/louis", base.resolve(other).toString());
    }

    // Ignore for now..
    @Ignore
    public void resolvePathSubdirsExtendsBeyondRoot() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("../random/../../../../../home");

        assertEquals("http://example.org/home", base.resolve(other).toString());
    }

    @Test
    public void resolvePathRelative() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("wee");

        assertEquals("http://example.org/foo/bar/wee", base.resolve(other).toString());
    }

    @Test
    public void resolvePathRelativeToNullPath() throws Exception {
        Uri base = new UriBuilder().setScheme("http").setAuthority("example.org").toUri();
        Uri other = Uri.parse("dir");

        assertEquals("http://example.org/dir", base.resolve(other).toString());
    }

    @Test
    public void resolvePathAbsolute() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("/blah");

        assertEquals("http://example.org/blah", base.resolve(other).toString());
    }

    @Test
    public void resolveAuthority() throws Exception {
        Uri base = Uri.parse("https://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("//example.com/blah");

        assertEquals("https://example.com/blah", base.resolve(other).toString());
    }

    @Test
    public void resolveAbsolute() throws Exception {
        Uri base = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri other = Uri.parse("http://www.ietf.org/rfc/rfc2396.txt");

        assertEquals("http://www.ietf.org/rfc/rfc2396.txt", base.resolve(other).toString());
    }

    @Test
    public void absoluteUrlIsAbsolute() {
        assertTrue("Url with scheme not reported absolute.", Uri.parse("http://example.org/foo").isAbsolute());
    }

    @Test
    public void relativeUrlIsNotAbsolute() {
        assertFalse("Url without scheme reported absolute.", Uri.parse("//example.org/foo").isAbsolute());
    }

    @Test
    public void parseWithSpecialCharacters() {
        String original = "http://example.org/?foo%25pbar=baz+blah";

        assertEquals(original, Uri.parse(original).toString());
    }

    @Test
    public void equalsAndHashCodeOk() {
        Uri uri = Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo");
        Uri uri2 = new UriBuilder().setScheme("http").setAuthority("example.org").setPath("/foo/bar/baz")
                .addQueryParameter("blah", "blah").setFragment("boo").toUri();

        assertEquals(uri, uri2);
        assertEquals(uri2, uri);

        assertEquals(uri.hashCode(), uri2.hashCode());
    }
    
    /** SBTWO-4172 */
    @Test
    public void fromJavaUriNasty() throws MalformedURLException {
        assertEquals(Uri.parse("http://go.warwick.ac.uk/pg/erefs%20.").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/pg/erefs .")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/elj/jilt/00%C3%A2%E2%82%AC%E2%80%9C2/collard.html").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/elj/jilt/00\u00e2\u20ac\u201c2/collard.html")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/pdfjam%22").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/pdfjam\"")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/dtu/pubs/wp/wp44/wp44%20.pdf").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/dtu/pubs/wp/wp44/wp44 .pdf")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/classical%20civilisation").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/classical civilisation")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/history%3E").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/history>")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/chemistry/%20research/chemicalbiology").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/chemistry/ research/chemicalbiology")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/insit%5De").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/insit]e")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/elj/jilt/cal/2%20jones/").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/elj/jilt/cal/2 jones/")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/elj/jilt/03-1/davies.htm%3Cbr%20/%3E").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/elj/jilt/03-1/davies.htm<br />")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/postgraduate%20applications").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/postgraduate applications")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/parishsymposium%3E").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/parishsymposium>")).toString());
//        assertEquals(Uri.parse("http://go.warwick.ac.uk/elj/jilt/...").toString(), Uri.fromJavaUrl(new URL("%0D%0Ahttp://www2.warwick.ac.uk/fac/sci/physics/teach/module_home/px121")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/%20masgak").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/ masgak")).toString());
        assertEquals(Uri.parse("http://www.go.warwick.ac.uk/elj/lgd/2008_1/d%C3%A2%E2%82%AC%E2%84%A2s%20ouza").toString(), Uri.fromJavaUrl(new URL("http://www.go.warwick.ac.uk/elj/lgd/2008_1/d\u00e2\u20ac\u2122s ouza")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/elj/jilt/01-3/byrnes.%20html/").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/elj/jilt/01-3/byrnes. html/")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/calendar/%20and%20the%20guidelines").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/calendar/ and the guidelines")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/%20ugfunding/2010-2011/mbchb").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/ ugfunding/2010-2011/mbchb")).toString());
        assertEquals(Uri.parse("http://www2.warwick.ac.uk/go/international/%3C!DOCTYPE").toString(), Uri.fromJavaUrl(new URL("http://www2.warwick.ac.uk/go/international/<!DOCTYPE")).toString());
        assertEquals(Uri.parse("http://www2.warwick.ac.uk/go/hefp/%3C!DOCTYPE").toString(), Uri.fromJavaUrl(new URL("http://www2.warwick.ac.uk/go/hefp/<!DOCTYPE")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/dtu/images/dtulogo%20(web).gif").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/dtu/images/dtulogo (web).gif")).toString());
        assertEquals(Uri.parse("http://www.go.warwick.ac.uk/shakespeare%22%3Ehttp:/go.warwick.ac.uk/shakespeare%3C/a%3E").toString(), Uri.fromJavaUrl(new URL("http://www.go.warwick.ac.uk/shakespeare\">http:/go.warwick.ac.uk/shakespeare</a>")).toString());
        assertEquals(Uri.parse("http://go.warwick.ac.uk/pgapply.%C3%82%20%C3%82").toString(), Uri.fromJavaUrl(new URL("http://go.warwick.ac.uk/pgapply.\u00c2 \u00c2 ")).toString());
    }
    
    @Test
    public void opaque() throws Exception {        
        assertEquals("mailto:m.mannion@warwick.ac.uk", Uri.parse("mailto:m.mannion@warwick.ac.uk").toString());
        assertEquals("gopher:something", Uri.parse("gopher:something").toString());
        assertEquals("ttp://www.google.com", Uri.parse("ttp://www.google.com").toString());
    }
}
