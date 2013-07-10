package uk.ac.warwick.util.content.texttransformers.embed;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.AbstractJUnit4JettyTest;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.content.texttransformers.embed.OEmbedProvider.Format;
import uk.ac.warwick.util.core.lookup.TwitterAuthenticationHttpRequestDecorator;
import uk.ac.warwick.util.web.Uri;

@SuppressWarnings("serial")
public class OEmbedTest extends AbstractJUnit4JettyTest {
    
    // Use the examples from https://dev.twitter.com/docs/auth/application-only-auth so we know the encoding is right
    private static final String TEST_TWITTER_CONSUMER_KEY = "xvz1evFS4wEEPTGEFPHBog";
    private static final String TEST_TWITTER_CONSUMER_SECRET = "L8qq9PZyRg6ieKGEKhZolGC0vJWLw8iEJ88DRdyOg";
    
    @BeforeClass
    public static void startServers() throws Exception {
        startServer(new HashMap<String, String>() {{
            put("/notfound.json", NotFoundServlet.class.getName());
            put("/unavailable.json", ServiceUnavailableServlet.class.getName());
            
            put("/youtube.json", YoutubeJSONOEmbedServlet.class.getName());
            put("/flickr.xml", FlickrXMLOEmbedServlet.class.getName());
            put("/flickr.json", FlickrJSONOEmbedServlet.class.getName());
            
            put("/twitter.json", TwitterJSONOEmbedServlet.class.getName());
            put("/twitteroauth2token", TwitterOAuthTokenServlet.class.getName());
        }});
    }
    
	@Test
	public void youtubeJson() throws OEmbedException {		
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("youtube")
					.withFormat(Format.json)
					.withMaxWidth(480)
					.withEndpoint(Uri.parse(super.serverAddress + "youtube.json"))
					.withUrlSchemes("http://(www|de)\\.youtube\\.com/watch\\?v=.*")
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://www.youtube.com/watch?v=lh_em3-ndVw"));
		assertEquals("http://www.youtube.com/user/queenofficial", response.getAuthorUrl());
		assertEquals("queenofficial", response.getAuthorName());
		assertEquals("1.0", response.getVersion());
		assertEquals("YouTube", response.getProviderName());
		assertEquals("http://www.youtube.com/", response.getProviderUrl());
		assertEquals("http://i1.ytimg.com/vi/lh_em3-ndVw/hqdefault.jpg", response.getThumbnailUrl());
		assertEquals("video", response.getType());
		assertEquals(480, response.getWidth().intValue());
		assertEquals(270, response.getHeight().intValue());
		assertEquals("Queen - 'Action This Day' (Live At The Bowl)", response.getTitle());
		assertEquals(480, response.getThumbnailWidth().intValue());
		assertEquals(360, response.getThumbnailHeight().intValue());
		assertEquals("<iframe width=\"480\" height=\"270\" src=\"http://www.youtube.com/embed/lh_em3-ndVw?feature=oembed\" frameborder=\"0\" allowfullscreen></iframe>", response.getHtml());
        assertEquals("youtube", response.getSource());
		
		assertNull(response.getCacheAge());
		assertNull(response.getUrl());
	}
	
	@Test
	public void flickrXml() throws OEmbedException {
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("flickr")
					.withFormat(Format.xml)
                    .withEndpoint(Uri.parse(super.serverAddress + "flickr.xml"))
					.withUrlSchemes("http://www\\.flickr\\.(com|de)/photos/.*")
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/caitysparkles/5263331070/"));
		assertEquals("http://www.flickr.com/photos/caitysparkles/", response.getAuthorUrl());
        assertEquals("caitysparkles", response.getAuthorName());
        assertEquals("1.0", response.getVersion());
        assertEquals("Flickr", response.getProviderName());
        assertEquals("http://www.flickr.com/", response.getProviderUrl());
        assertEquals("http://farm6.staticflickr.com/5207/5263331070_2c5b799c88_s.jpg", response.getThumbnailUrl());
        assertEquals("photo", response.getType());
        assertEquals(1024, response.getWidth().intValue());
        assertEquals(683, response.getHeight().intValue());
        assertEquals("Ngawi", response.getTitle());
        assertEquals(75, response.getThumbnailWidth().intValue());
        assertEquals(75, response.getThumbnailHeight().intValue());
        assertEquals("flickr", response.getSource());
        
        assertEquals(3600L, response.getCacheAge().longValue());
        assertEquals("http://farm6.staticflickr.com/5207/5263331070_2c5b799c88_b.jpg", response.getUrl());
        assertNull(response.getHtml());
	}
	
	@Test
	public void flickrJson() throws OEmbedException {
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("flickr")
					.withFormat(Format.json)
                    .withEndpoint(Uri.parse(super.serverAddress + "flickr.json"))
					.withUrlSchemes("http://www\\.flickr\\.(com|de)/photos/.*")
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/caitysparkles/5263331070/"));
		assertEquals("http://www.flickr.com/photos/caitysparkles/", response.getAuthorUrl());
        assertEquals("caitysparkles", response.getAuthorName());
        assertEquals("1.0", response.getVersion());
        assertEquals("Flickr", response.getProviderName());
        assertEquals("http://www.flickr.com/", response.getProviderUrl());
        assertEquals("http://farm6.staticflickr.com/5207/5263331070_2c5b799c88_s.jpg", response.getThumbnailUrl());
        assertEquals("photo", response.getType());
        assertEquals(1024, response.getWidth().intValue());
        assertEquals(683, response.getHeight().intValue());
        assertEquals("Ngawi", response.getTitle());
        assertEquals(75, response.getThumbnailWidth().intValue());
        assertEquals(75, response.getThumbnailHeight().intValue());
        assertEquals("flickr", response.getSource());
        
        assertEquals(3600L, response.getCacheAge().longValue());
        assertEquals("http://farm6.staticflickr.com/5207/5263331070_2c5b799c88_b.jpg", response.getUrl());
        assertNull(response.getHtml());
	}
	
	@Test
	public void twitterJson() throws OEmbedException {
	    final TwitterAuthenticationHttpRequestDecorator decorator = 
            new TwitterAuthenticationHttpRequestDecorator(Uri.parse(super.serverAddress + "twitteroauth2token"));
	    decorator.setConsumerKey(TEST_TWITTER_CONSUMER_KEY);
	    decorator.setConsumerSecret(TEST_TWITTER_CONSUMER_SECRET);
	    
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("twitter")
					.withFormat(Format.json)
					.withMaxWidth(480)
                    .withEndpoint(Uri.parse(super.serverAddress + "twitter.json"))
					.withUrlSchemes("https?://twitter.com/#!/[a-z0-9_]{1,20}/status/\\d+")
					.withHttpRequestDecorator(decorator)
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://twitter.com/#!/twitterapi/status/144840776101273600"));
        assertEquals("https://twitter.com/twitterapi", response.getAuthorUrl());
        assertEquals("Twitter API", response.getAuthorName());
        assertEquals("1.0", response.getVersion());
        assertEquals("Twitter", response.getProviderName());
        assertEquals("https://twitter.com", response.getProviderUrl());
        assertEquals("rich", response.getType());
        assertEquals(480, response.getWidth().intValue());
        assertEquals("twitter", response.getSource());
        assertEquals(3153600000L, response.getCacheAge().longValue());
        assertEquals("https://twitter.com/twitterapi/statuses/144840776101273600", response.getUrl());
        assertEquals("<blockquote class=\"twitter-tweet\" width=\"480\"><p>Introducing the statuses/oembed endpoint: <a href=\"http://t.co/vQGXtst9\">http://t.co/vQGXtst9</a> ^TS</p>&mdash; Twitter API (@twitterapi) <a href=\"https://twitter.com/twitterapi/statuses/144840776101273600\">December 8, 2011</a></blockquote>\n<script async src=\"//platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>", response.getHtml());
        
        assertNull(response.getTitle());
        assertNull(response.getHeight());
        assertNull(response.getThumbnailUrl());
        assertNull(response.getThumbnailWidth());
        assertNull(response.getThumbnailHeight());
	}
	
	@Test
	@Ignore // TODO need an autodiscovery test
	public void dailyfratze() throws OEmbedException {
		final OEmbedImpl oembed = new OEmbedImpl();
		oembed.setAutodiscovery(true);
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://dailyfratze.de/michael/2010/8/22"));
		System.out.println(response);
	}
	
	@Test
    @Ignore // TODO need an autodiscovery test
	public void dailyfratzeThroughCache() throws OEmbedException {
		final OEmbed oembed = new OEmbedBuilder()
			.withAutodiscovery(true)
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("flickr")
					.withFormat(Format.xml)
					.withEndpoint(Uri.parse("http://www.flickr.com/services/oembed"))
					.withUrlSchemes("http://www\\.flickr\\.(com|de)/photos/.*")
					.build()
				)
			.build();
		
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://dailyfratze.de/michael/2010/8/23"));
		System.out.println(response);
		
		response = oembed.transformUrl(Uri.parse("http://dailyfratze.de/michael/2010/8/23"));
		System.out.println(response);
				
		// 404 etc. is not called twice		
		response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/idontexists/123456/"));
		assertTrue(response.isEmpty());
		
		response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/idontexists/123456/"));		
		assertTrue(response.isEmpty());
	}
	
	@Test
	public void jsonMarshall() throws OEmbedException {
		OEmbedResponse oembedResponse = new OEmbedResponse();
		oembedResponse.setHtml("<div>foobar</div>");
		oembedResponse.setSource("Should be ignored");
		oembedResponse.setAuthorName("Michael Simons");		
		OEmbedJsonParser p = new OEmbedJsonParser();
		assertEquals("{\"html\":\"<div>foobar</div>\",\"author_name\":\"Michael Simons\"}", p.marshal(oembedResponse));
	}
    
    @BeforeClass
    public static void setupEhCache() throws Exception {
        Caches.resetEhCacheCheck();
        System.setProperty("warwick.ehcache.disk.store.dir", root.getAbsolutePath());
    }
    
    @AfterClass
    public static void unsetEhCache() throws Exception {
        System.clearProperty("warwick.ehcache.disk.store.dir");
        Caches.resetEhCacheCheck();
    }
    
    @After
    public void emptyCache() {
        assertTrue(Caches.newCache(OEmbed.CACHE_NAME, null, 0, CacheStrategy.EhCacheRequired).clear());
    }
    
    public static class TwitterOAuthTokenServlet extends HttpServlet {
        
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("Basic eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==", req.getHeader("Authorization"));
            assertEquals("application/x-www-form-urlencoded;charset=UTF-8", req.getHeader("Content-Type"));
            
            assertEquals("client_credentials", req.getParameter("grant_type"));
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"token_type\":\"bearer\",\"access_token\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2FAAAAAAAAAAAAAAAAAAAA%3DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}");
        }
    }
    
    public static class YoutubeJSONOEmbedServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json+oembed");
            
            FileCopyUtils.copy(getClass().getResourceAsStream("/oembed/youtube.json"), resp.getOutputStream());
        }
    }
    
    public static class FlickrXMLOEmbedServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/xml+oembed");
            
            FileCopyUtils.copy(getClass().getResourceAsStream("/oembed/flickr.xml"), resp.getOutputStream());
        }
    }
    
    public static class FlickrJSONOEmbedServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json+oembed");
            
            FileCopyUtils.copy(getClass().getResourceAsStream("/oembed/flickr.json"), resp.getOutputStream());
        }
    }
    
    public static class TwitterJSONOEmbedServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertEquals("Bearer AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2FAAAAAAAAAAAAAAAAAAAA%3DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", req.getHeader("Authorization"));
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json+oembed");
            
            FileCopyUtils.copy(getClass().getResourceAsStream("/oembed/twitter.json"), resp.getOutputStream());
        }
    }
}