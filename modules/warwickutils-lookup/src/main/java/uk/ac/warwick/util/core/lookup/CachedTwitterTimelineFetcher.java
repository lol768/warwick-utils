package uk.ac.warwick.util.core.lookup;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.concurrency.promise.UnfulfilledPromiseException;
import uk.ac.warwick.util.concurrency.promise.WriteOncePromise;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.HttpRequestDecorator;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

public final class CachedTwitterTimelineFetcher implements TwitterTimelineFetcher, InitializingBean {
    
    public static final String CACHE_NAME = "TwitterTimelineCache";
    
    public static final long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 1; // Cache for one hour
    
    private static final int TWEET_COUNT_PADDING = 20;
    
    private static final Uri DEFAULT_BASE_URL = Uri.parse("https://api.twitter.com/1.1/statuses/user_timeline.json");
    
    private final Uri baseUri;
    
    private final WriteOncePromise<Cache<Uri, TwitterTimelineResponse>> cache = new WriteOncePromise<Cache<Uri,TwitterTimelineResponse>>();
    
    private HttpRequestDecorator httpRequestDecorator;
    
    public CachedTwitterTimelineFetcher() {
        this(DEFAULT_BASE_URL);
    }
    
    CachedTwitterTimelineFetcher(Uri theBaseUri) {
        this.baseUri = theBaseUri;
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

	public synchronized void initialiseCache() {
    	if (this.cache.isWritten()) return;
    	
    	this.cache.setValue(Caches.newCache(CACHE_NAME, new TwitterTimelineEntryFactory(httpRequestDecorator), DEFAULT_CACHE_TIMEOUT, CacheStrategy.EhCacheRequired));
    }
    
    public HttpRequestDecorator getHttpRequestDecorator() {
        return httpRequestDecorator;
    }

    public void setHttpRequestDecorator(HttpRequestDecorator httpRequestDecorator) {
        this.httpRequestDecorator = httpRequestDecorator;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(httpRequestDecorator, "httpRequestDecorator must be set! (see TwitterAuthenticationHttpRequestDecorator)");
    }

    private static final class TwitterTimelineEntryFactory extends SingularCacheEntryFactory<Uri, TwitterTimelineResponse> {
    	
        private final HttpRequestDecorator httpRequestDecorator;
    	
    	private TwitterTimelineEntryFactory(HttpRequestDecorator decorator) {
    		this.httpRequestDecorator = decorator;
    	}

        public TwitterTimelineResponse create(Uri uri) throws CacheEntryUpdateException {
            HttpMethodExecutor executor = new SimpleHttpMethodExecutor(Method.get);
            executor.setUrl(uri);
            executor.setHttpRequestDecorator(httpRequestDecorator);
            
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
