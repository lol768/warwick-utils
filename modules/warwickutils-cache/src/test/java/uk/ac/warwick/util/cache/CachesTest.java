package uk.ac.warwick.util.cache;

import org.junit.Test;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.caffeine.CaffeineCacheStore;
import uk.ac.warwick.util.cache.memcached.MemcachedCacheStore;

import static org.junit.Assert.*;

public class CachesTest {
    
    private static final String CACHE_NAME = "customCache";

	@Test
	public void getCaffeine() {
		CacheStore<?, ?> store = Caches.builder(CACHE_NAME, CacheStrategy.CaffeineIfAvailable).maximumSize(100).buildStore();
		assertTrue(store instanceof CaffeineCacheStore<?,?>);
	}
	
	@Test
	public void getMemcache() {
		CacheStore<?, ?> store = Caches.builder(CACHE_NAME, CacheStrategy.MemcachedIfAvailable).buildStore();
		assertTrue(store instanceof MemcachedCacheStore<?,?>);
    }
	
	@Test
	public void getInMemoryCache() {
        CacheStore<?, ?> store = Caches.builder(CACHE_NAME, CacheStrategy.InMemoryOnly).buildStore();
        assertTrue(store instanceof HashMapCacheStore<?,?>);
    }
}
