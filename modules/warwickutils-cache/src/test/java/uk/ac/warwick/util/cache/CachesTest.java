package uk.ac.warwick.util.cache;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.ehcache.EhCacheStore;

public class CachesTest {
    
    private static final String CACHE_NAME = "customCache";
    
	@Before
	public void setUp() {
		EhCacheUtils.setUp();
	}	
	
	@After
	public void tearDown() {
		EhCacheUtils.tearDown();
	}
	
	/**
	 * Should return false as we haven't specified a disk cache location in a system property.
	 */
	@Test
	public void getAvailableCache() throws Exception {
		CacheStore<String,String> store = Caches.<String,String>newCacheStore(CACHE_NAME, 100, CacheStrategy.EhCacheIfAvailable);
		assertFalse(store instanceof EhCacheStore<?,?>);
	}
	
	@Test
	public void getEhCache() throws Exception {
	    try {
	        Caches.<String,String>newCacheStore(CACHE_NAME, 100, CacheStrategy.EhCacheRequired);
	        fail("Expected exception");
	    } catch (IllegalStateException e) {
	        // expected
	    }
    }
	
	@Test
	public void getInMemoryCache() throws Exception {
        CacheStore<String,String> store = Caches.<String,String>newCacheStore(CACHE_NAME, 100, CacheStrategy.InMemoryOnly);
        assertTrue(store instanceof HashMapCacheStore<?,?>);
    }
}
