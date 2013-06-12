package uk.ac.warwick.util.core.lookup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.concurrency.promise.UnfulfilledPromiseException;
import uk.ac.warwick.util.concurrency.promise.WriteOncePromise;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

public final class CachedTwitterTimelineFetcher implements TwitterTimelineFetcher {
    
    public static final String CACHE_NAME = "TwitterTimelineCache";
    
    public static final long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 1; // Cache for one hour
    
    private static final int TWEET_COUNT_PADDING = 20;
    
    private static final Uri DEFAULT_BASE_URL = Uri.parse("https://api.twitter.com/1.1/statuses/user_timeline.json");
    
    private static final Uri DEFAULT_OAUTH_ENDPOINT_URL = Uri.parse("https://api.twitter.com/oauth2/token");
    
    private final Uri baseUri;
    
    private final Uri oauthEndpointUri;
    
    private final WriteOncePromise<Cache<Uri, TwitterTimelineResponse>> cache = new WriteOncePromise<Cache<Uri,TwitterTimelineResponse>>();
    
    private final String consumerKey;
    
    private final String consumerSecret;
    
    public CachedTwitterTimelineFetcher(String theConsumerKey, String theConsumerSecret) {
        this(theConsumerKey, theConsumerSecret, DEFAULT_BASE_URL, DEFAULT_OAUTH_ENDPOINT_URL);
    }
    
    public CachedTwitterTimelineFetcher(String theConsumerKey, String theConsumerSecret, Uri theBaseUri, Uri theOAuthEndpointUri) {
    	Assert.isTrue(StringUtils.hasText(theConsumerKey), "Consumer key must be specified");
    	Assert.isTrue(StringUtils.hasText(theConsumerSecret), "Consumer secret must be specified");
    	
        this.baseUri = theBaseUri;
        this.oauthEndpointUri = theOAuthEndpointUri;
        this.consumerKey = theConsumerKey;
        this.consumerSecret = theConsumerSecret;
    }
    
    public synchronized void initialiseCache() {
    	if (this.cache.isWritten()) return;
    	
    	// Generate a bearer token. Ref https://dev.twitter.com/docs/auth/application-only-auth
    	try {
	    	String encodedKey = URLEncoder.encode(consumerKey, "UTF-8");
	    	String encodedSecret = URLEncoder.encode(consumerSecret, "UTF-8");
	    	
	    	String bearerCredentials = String.format("%s:%s", encodedKey, encodedSecret);
	    	String encodedBearerCredentials = new String(Base64.encodeBase64(bearerCredentials.getBytes("UTF-8")), "UTF-8");
	    	
	    	HttpMethodExecutor executor = new SimpleHttpMethodExecutor(Method.post);
            executor.setUrl(oauthEndpointUri);
            executor.addHeader("Authorization", "Basic " + encodedBearerCredentials);
            executor.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            
            executor.setPostBody(Collections.singletonList(new BasicNameValuePair("grant_type", "client_credentials")));
            
            String bearerToken = executor.execute(new ResponseHandler<String>() {
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					String responseText = EntityUtils.toString(response.getEntity());
					
					if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
						throw new IllegalStateException(
							String.format("Expected HTTP 200 from Twitter OAuth 2 endpoint; received %d (content %s)", response.getStatusLine().getStatusCode(), responseText)
						);
					}
					
					try {
						JSONObject json = new JSONObject(responseText);
						
						String tokenType = json.getString("token_type");
						if (!"bearer".equals(tokenType)) {
							throw new IllegalStateException(String.format("Expected bearer token from Twitter API (was %s)", tokenType));
						}
						
						return json.getString("access_token");
					} catch (JSONException e) {
						throw new IllegalStateException(String.format("Invalid JSON received from Twitter API (received %s)", responseText), e);
					}
				}
            }).getRight();
            
            this.cache.setValue(Caches.newCache(CACHE_NAME, new TwitterTimelineEntryFactory(bearerToken), DEFAULT_CACHE_TIMEOUT, CacheStrategy.EhCacheRequired));
    	} catch (UnsupportedEncodingException e) {
    		throw new IllegalStateException("UTF-8 no longer supported!?!!??!", e);
    	} catch (IOException e) {
    		throw new IllegalStateException("Couldn't fetch bearer token from Twitter OAuth!", e);
    	}
    }
        
    public TwitterTimelineResponse get(String accountName, int num, boolean includeRetweets, boolean excludeReplies) throws CacheEntryUpdateException {
    	initialiseCache();
    	
        UriBuilder uri = new UriBuilder(baseUri);
        uri.addQueryParameter("screen_name", accountName.toLowerCase());
        uri.addQueryParameter("count", Integer.toString(num + TWEET_COUNT_PADDING));
        uri.addQueryParameter("include_rts", Boolean.toString(includeRetweets));
        uri.addQueryParameter("exclude_replies", Boolean.toString(excludeReplies));
        
        // entities are on by default in API version 1.1 so don't need to include_entities
        
        try {
        	return cache.fulfilPromise().get(uri.toUri());
        } catch (UnfulfilledPromiseException e) {
        	// Should never happen - we've fulfilled it at the start of the method
        	throw new IllegalStateException(e);
        }
    }
    
    private static final class TwitterTimelineEntryFactory extends SingularCacheEntryFactory<Uri, TwitterTimelineResponse> {
    	
    	private final String bearerToken;
    	
    	private TwitterTimelineEntryFactory(String token) {
    		this.bearerToken = token;
    	}

        public TwitterTimelineResponse create(Uri uri) throws CacheEntryUpdateException {
            HttpMethodExecutor executor = new SimpleHttpMethodExecutor(Method.get);
            executor.setUrl(uri);
            executor.addHeader("Authorization", "Bearer " + bearerToken);
            
            try {
                return executor.execute(new ResponseHandler<TwitterTimelineResponse>() {
                    public TwitterTimelineResponse handleResponse(HttpResponse resp) throws IOException {
                        return new TwitterTimelineResponse(
                            EntityUtils.toString(resp.getEntity()),
                            resp.getStatusLine().getStatusCode(),
                            resp.getAllHeaders()
                        );
                    }
                }).getRight();
            } catch (IOException e) {
                throw new CacheEntryUpdateException(e);
            }
        }

        public boolean shouldBeCached(TwitterTimelineResponse entry) {
            return entry.getStatusCode() != HttpStatus.SC_SERVICE_UNAVAILABLE;
        }
        
    }

}
