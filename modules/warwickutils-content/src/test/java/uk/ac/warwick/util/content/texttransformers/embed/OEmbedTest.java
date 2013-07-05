package uk.ac.warwick.util.content.texttransformers.embed;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.warwick.util.content.texttransformers.embed.OEmbedProvider.Format;
import uk.ac.warwick.util.core.lookup.TwitterAuthenticationHttpRequestDecorator;
import uk.ac.warwick.util.web.Uri;

/**
 * FIXME Not a real test but a simple demonstration how to use OEmbed
 */
public class OEmbedTest {
	@Test
	public void youtubeJson() throws OEmbedException {		
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("youtube")
					.withFormat(Format.json)
					.withMaxWidth(480)
					.withEndpoint(Uri.parse("http://www.youtube.com/oembed"))
					.withUrlSchemes("http://(www|de)\\.youtube\\.com/watch\\?v=.*")
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://www.youtube.com/watch?v=lh_em3-ndVw"));
		System.out.println(response);
	}
	
	@Test
	public void flickrXml() throws OEmbedException {
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("flickr")
					.withFormat(Format.xml)
					.withEndpoint(Uri.parse("http://www.flickr.com/services/oembed"))
					.withUrlSchemes("http://www\\.flickr\\.(com|de)/photos/.*")
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/caitysparkles/5263331070/"));
		System.out.println(response);
	}
	
	@Test
	public void flickrJson() throws OEmbedException {
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("flickr")
					.withFormat(Format.json)
					.withEndpoint(Uri.parse("http://www.flickr.com/services/oembed"))
					.withUrlSchemes("http://www\\.flickr\\.(com|de)/photos/.*")
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/pierrehanquin/6948565021"));
		System.out.println(response);
	}
	
	@Test
	public void twitterJson() throws OEmbedException {
	    final TwitterAuthenticationHttpRequestDecorator decorator = new TwitterAuthenticationHttpRequestDecorator();
	    decorator.setConsumerKey("--- consumer key ---");
	    decorator.setConsumerSecret("--- consumer secret ---");
	    
		final OEmbed oembed = new OEmbedBuilder()
			.withProviders(
				new OEmbedProviderBuilder()
					.withName("twitter")
					.withFormat(Format.json)
					.withMaxWidth(480)
					.withEndpoint(Uri.parse("https://api.twitter.com/1.1/statuses/oembed.%{format}"))
					.withUrlSchemes("https?://twitter.com/#!/[a-z0-9_]{1,20}/status/\\d+")
					.withHttpRequestDecorator(decorator)
					.build()
				)
			.build();
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://twitter.com/#!/twitterapi/status/144840776101273600"));
		System.out.println(response);
	}
	
	@Test
	public void dailyfratze() throws OEmbedException {
		final OEmbed oembed = new OEmbed();
		oembed.setAutodiscovery(true);
		OEmbedResponse response = oembed.transformUrl(Uri.parse("http://dailyfratze.de/michael/2010/8/22"));
		System.out.println(response);
	}
	
	@Test
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
		Assert.assertTrue(response.isEmpty());
		
		response = oembed.transformUrl(Uri.parse("http://www.flickr.com/photos/idontexists/123456/"));		
		Assert.assertTrue(response.isEmpty());
	}
	
	@Test
	public void jsonMarshall() throws OEmbedException {
		OEmbedResponse oembedResponse = new OEmbedResponse();
		oembedResponse.setHtml("<div>foobar</div>");
		oembedResponse.setSource("Should be ignored");
		oembedResponse.setAuthorName("Michael Simons");		
		OEmbedJsonParser p = new OEmbedJsonParser();
		System.out.println(p.marshal(oembedResponse));
	}
}