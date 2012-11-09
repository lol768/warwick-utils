package uk.ac.warwick.util.core.lookup;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

public final class CachedTwitterTimelineFetcher implements TwitterTimelineFetcher {
    
    public static final String CACHE_NAME = "TwitterTimelineCache";
    
    public static final long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 1; // Cache for one hour
    
    private static final int TWEET_COUNT_PADDING = 20;
    
    private static final Uri DEFAULT_BASE_URL = Uri.parse("http://api.twitter.com/1/statuses/user_timeline/");
    
    private final Cache<Uri, TwitterTimelineResponse> cache;
    
    private final Uri baseUri;
    
    public CachedTwitterTimelineFetcher() {
        this(DEFAULT_BASE_URL);
    }
    
    public CachedTwitterTimelineFetcher(Uri theBaseUri) {
        this.baseUri = theBaseUri;
        this.cache = Caches.newCache(CACHE_NAME, new TwitterTimelineEntryFactory(), DEFAULT_CACHE_TIMEOUT, CacheStrategy.EhCacheRequired);
    }
        
    public TwitterTimelineResponse get(String accountName, int num, boolean includeRetweets) throws CacheEntryUpdateException {
        UriBuilder uri = new UriBuilder(baseUri);
        uri.setPath(uri.getPath() + accountName.toLowerCase() + ".json");
        uri.addQueryParameter("count", Integer.toString(num + TWEET_COUNT_PADDING));
        uri.addQueryParameter("include_entities", "true");
        uri.addQueryParameter("include_rts", Boolean.toString(includeRetweets));
        
        return cache.get(uri.toUri());
    }
    
    private static final class TwitterTimelineEntryFactory extends SingularCacheEntryFactory<Uri, TwitterTimelineResponse> {

        public TwitterTimelineResponse create(Uri uri) throws CacheEntryUpdateException {
            HttpMethodExecutor executor = new SimpleHttpMethodExecutor(Method.get);
            executor.setUrl(uri);
            
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
