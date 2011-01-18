/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 */
package uk.ac.warwick.util.web;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Tests for UriBuilder
 */
public class UriBuilderTest {

	@Test
	public void allPartsUsed() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?hello=world#foo", builder.toString());
	}

	@Test
	public void noSchemeUsed() {
		UriBuilder builder = new UriBuilder()
				.setAuthority("apache.org")
				.setPath("/shindig")
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("//apache.org/shindig?hello=world#foo", builder.toString());
	}

	@Test
	public void noAuthorityUsed() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setPath("/shindig")
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("http:/shindig?hello=world#foo", builder.toString());
	}

	@Test
	public void noPathUsed() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("http://apache.org?hello=world#foo", builder.toString());
	}

	@Test
	public void noQueryUsed() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.setFragment("foo");
		assertEquals("http://apache.org/shindig#foo", builder.toString());
	}

	@Test
	public void noFragmentUsed() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.setQuery("hello=world");
		assertEquals("http://apache.org/shindig?hello=world", builder.toString());
	}

	@Test
	public void hostRelativePaths() {
		UriBuilder builder = new UriBuilder()
				.setPath("/shindig")
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("/shindig?hello=world#foo", builder.toString());
	}

	@Test
	public void relativePaths() {
		UriBuilder builder = new UriBuilder()
				.setPath("foo")
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("foo?hello=world#foo", builder.toString());
	}

	@Test
	public void noPathNoHostNoAuthority() {
		UriBuilder builder = new UriBuilder()
				.setQuery("hello=world")
				.setFragment("foo");
		assertEquals("?hello=world#foo", builder.toString());
	}

	@Test
	public void justSchemeAndAuthority() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org");
		assertEquals("http://apache.org", builder.toString());
	}

	@Test
	public void justPath() {
		UriBuilder builder = new UriBuilder()
				.setPath("/shindig");
		assertEquals("/shindig", builder.toString());
	}

	@Test
	public void justAuthorityAndPath() {
		UriBuilder builder = new UriBuilder()
				.setAuthority("apache.org")
				.setPath("/shindig");
		assertEquals("//apache.org/shindig", builder.toString());
	}

	@Test
	public void justQuery() {
		UriBuilder builder = new UriBuilder()
				.setQuery("hello=world");
		assertEquals("?hello=world", builder.toString());
	}

	@Test
	public void justFragment() {
		UriBuilder builder = new UriBuilder()
				.setFragment("foo");
		assertEquals("#foo", builder.toString());
	}

	@Test
	public void addSingleQueryParameter() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addQueryParameter("hello", "world")
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?hello=world#foo", builder.toString());
	}

	@Test
	public void addTwoQueryParameters() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addQueryParameter("hello", "world")
				.addQueryParameter("foo", "bar")
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?hello=world&foo=bar#foo", builder.toString());
	}

	@Test
	public void iterableQueryParameters() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.putQueryParameter("hello", Lists.newArrayList("world", "monde"))
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?hello=world&hello=monde#foo", builder.toString());
	}
 
	@Test
	public void removeQueryParameter() {
		UriBuilder uri = UriBuilder.parse("http://www.example.com/foo?bar=baz&quux=baz");
		uri.removeQueryParameter("bar");
		assertEquals("http://www.example.com/foo?quux=baz", uri.toString());
		uri.removeQueryParameter("quux");
		assertEquals("http://www.example.com/foo", uri.toString());
	}

	@Test
	public void addIdenticalParameters() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addQueryParameter("hello", "world")
				.addQueryParameter("hello", "goodbye")
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?hello=world&hello=goodbye#foo", builder.toString());
	}

	@Test
	public void addBatchParameters() {
		Map<String, String> params = Maps.newLinkedHashMap();
		params.put("foo", "bar");
		params.put("hello", "world");
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addQueryParameters(params)
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?foo=bar&hello=world#foo", builder.toString());
	}

	@Test
	public void queryStringIsUnescaped() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.setQuery("hello+world=world%26bar");
		assertEquals("world&bar", builder.getQueryParameter("hello world"));
	}

	@Test
	public void queryParamsAreEscaped() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addQueryParameter("hello world", "foo&bar")
				.setFragment("foo");
		assertEquals("http://apache.org/shindig?hello+world=foo%26bar#foo", builder.toString());
		assertEquals("hello+world=foo%26bar", builder.getQuery());
	}
	
	@Test
	public void addSingleFragmentParameter() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addFragmentParameter("hello", "world")
				.setQuery("foo");
		assertEquals("http://apache.org/shindig?foo#hello=world", builder.toString());
	}

	@Test
	public void addTwoFragmentParameters() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addFragmentParameter("hello", "world")
				.addFragmentParameter("foo", "bar")
				.setQuery("foo");
		assertEquals("http://apache.org/shindig?foo#hello=world&foo=bar", builder.toString());
	}

	@Test
	public void iterableFragmentParameters() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.putFragmentParameter("hello", Lists.newArrayList("world", "monde"))
				.setQuery("foo");
		assertEquals("http://apache.org/shindig?foo#hello=world&hello=monde", builder.toString());
	}
 
	@Test
	public void removeFragmentParameter() {
		UriBuilder uri = UriBuilder.parse("http://www.example.com/foo#bar=baz&quux=baz");
		uri.removeFragmentParameter("bar");
		assertEquals("http://www.example.com/foo#quux=baz", uri.toString());
		uri.removeFragmentParameter("quux");
		assertEquals("http://www.example.com/foo", uri.toString());
	}

	@Test
	public void addIdenticalFragmentParameters() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addFragmentParameter("hello", "world")
				.addFragmentParameter("hello", "goodbye")
				.setQuery("foo");
		assertEquals("http://apache.org/shindig?foo#hello=world&hello=goodbye", builder.toString());
	}

	@Test
	public void addBatchFragmentParameters() {
		Map<String, String> params = Maps.newLinkedHashMap();
		params.put("foo", "bar");
		params.put("hello", "world");
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addFragmentParameters(params)
				.setQuery("foo");
		assertEquals("http://apache.org/shindig?foo#foo=bar&hello=world", builder.toString());
	}

	@Test
	public void fragmentStringIsUnescaped() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.setFragment("hello+world=world%26bar");
		assertEquals("world&bar", builder.getFragmentParameter("hello world"));
	}

	@Test
	public void fragmentParamsAreEscaped() {
		UriBuilder builder = new UriBuilder()
				.setScheme("http")
				.setAuthority("apache.org")
				.setPath("/shindig")
				.addFragmentParameter("hello world", "foo&bar")
				.setQuery("foo");
		assertEquals("http://apache.org/shindig?foo#hello+world=foo%26bar", builder.toString());
		assertEquals("hello+world=foo%26bar", builder.getFragment());
	}

	@Test
	public void parse() {
		UriBuilder builder = UriBuilder.parse("http://apache.org/shindig?foo=bar%26baz&foo=three#blah");

		assertEquals("http", builder.getScheme());
		assertEquals("apache.org", builder.getAuthority());
		assertEquals("/shindig", builder.getPath());
		assertEquals("foo=bar%26baz&foo=three", builder.getQuery());
		assertEquals("blah", builder.getFragment());

		assertEquals("bar&baz", builder.getQueryParameter("foo"));

		List<String> values = Arrays.asList("bar&baz", "three");
		assertEquals(values, builder.getQueryParameters("foo"));
	}

	@Test
	public void constructFromUriAndBack() {
		Uri uri = Uri.parse("http://apache.org/foo/bar?foo=bar&a=b&c=d&y=z&foo=zoo#foo");
		UriBuilder builder = new UriBuilder(uri);

		assertEquals(uri, builder.toUri());
	}

	@Test
	public void constructFromUriAndModify() {
		Uri uri = Uri.parse("http://apache.org/foo/bar?foo=bar#foo");
		UriBuilder builder = new UriBuilder(uri);

		builder.setAuthority("example.org");
		builder.addQueryParameter("bar", "foo");

		assertEquals("http://example.org/foo/bar?foo=bar&bar=foo#foo", builder.toString());
	}

	@Test
	public void equalsAndHashCodeOk() {
		UriBuilder uri = UriBuilder.parse("http://example.org/foo/bar/baz?blah=blah#boo");
		UriBuilder uri2 = new UriBuilder(Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo"));

		assertEquals(uri, uri2);
		assertEquals(uri2, uri);

		assertEquals(uri, uri);

		assertNotNull(uri);
		assertNotEquals(uri, "http://example.org/foo/bar/baz?blah=blah#boo");
		assertNotEquals(uri, Uri.parse("http://example.org/foo/bar/baz?blah=blah#boo"));

		assertEquals(uri.hashCode(), uri2.hashCode());
	}
	
	private final Mockery m = new JUnit4Mockery();
	
	@Test
	public void constructFromServletRequestHttpStandardPortAndModify() {
		final HttpServletRequest req = m.mock(HttpServletRequest.class);
		
		m.checking(new Expectations() {{
		    one(req).getScheme(); will(returnValue("http"));
		    atLeast(1).of(req).getServerName(); will(returnValue("example.com"));
		    one(req).getServerPort(); will(returnValue(80));
		    atLeast(1).of(req).getRequestURI(); will(returnValue("/my/path"));
		    atLeast(1).of(req).getQueryString(); will(returnValue("foo=bar&baz=bak"));
		}});
		
		UriBuilder builder = new UriBuilder(req);
		m.assertIsSatisfied();
		
		assertEquals("http://example.com/my/path?foo=bar&baz=bak", builder.toString());
		assertEquals("bar", builder.getQueryParameter("foo"));  // sanity check on a single param
		assertEquals(0, builder.getFragmentParameters().size());  // shouldn't NPE
		
		// Simple modification
		builder.setPath("/other/path");
		assertEquals("http://example.com/other/path?foo=bar&baz=bak", builder.toString());
	}
	
	@Test
	public void constructFromServletRequestHttpsStandardPort() {
	    final HttpServletRequest req = m.mock(HttpServletRequest.class);
        
        m.checking(new Expectations() {{
            one(req).getScheme(); will(returnValue("https"));
            atLeast(1).of(req).getServerName(); will(returnValue("example.com"));
            one(req).getServerPort(); will(returnValue(443));
            atLeast(1).of(req).getRequestURI(); will(returnValue("/my/path"));
            atLeast(1).of(req).getQueryString(); will(returnValue("foo=bar&baz=bak"));
        }});
		
		UriBuilder builder = new UriBuilder(req);
		m.assertIsSatisfied();
		
		assertEquals("https://example.com/my/path?foo=bar&baz=bak", builder.toString());
	}
	
	@Test
	public void constructFromServletRequestNonStandardPort() {
	    final HttpServletRequest req = m.mock(HttpServletRequest.class);
        
        m.checking(new Expectations() {{
            one(req).getScheme(); will(returnValue("HtTp"));
            atLeast(1).of(req).getServerName(); will(returnValue("example.com"));
            one(req).getServerPort(); will(returnValue(5000));
            atLeast(1).of(req).getRequestURI(); will(returnValue("/my/path"));
            atLeast(1).of(req).getQueryString(); will(returnValue("one=two&three=four"));
        }});
		
		UriBuilder builder = new UriBuilder(req);
		m.assertIsSatisfied();
		
		assertEquals("http://example.com:5000/my/path?one=two&three=four", builder.toString());
	}
	
	@Test
	public void constructFromServletRequestNonePort() {
	    final HttpServletRequest req = m.mock(HttpServletRequest.class);
        
        m.checking(new Expectations() {{
            one(req).getScheme(); will(returnValue("http"));
            atLeast(1).of(req).getServerName(); will(returnValue("example.com"));
            one(req).getServerPort(); will(returnValue(-1));
            atLeast(1).of(req).getRequestURI(); will(returnValue("/my/path"));
            atLeast(1).of(req).getQueryString(); will(returnValue("one=two&three=four"));
        }});
		
		UriBuilder builder = new UriBuilder(req);
		m.assertIsSatisfied();
		
		assertEquals("http://example.com/my/path?one=two&three=four", builder.toString());
	}
	
	public static void assertNotEquals(Object expected, Object actual) {
	    assertNotEquals(null, expected, actual);
	}
	
	public static void assertNotEquals(String message, Object expected, Object actual) {
	    if (expected == null || !expected.equals(actual)) {
            return;
	    } else {
	        String formatted= "";
	        if (message != null && !message.equals(""))
	            formatted= message + " ";
	        String expectedString = String.valueOf(expected);
	        String actualString = String.valueOf(actual);
	        
	        fail(formatted + "expected:<" + expectedString + "> to be not equal to:<"
                    + actualString + ">");
	    }
    }
}
