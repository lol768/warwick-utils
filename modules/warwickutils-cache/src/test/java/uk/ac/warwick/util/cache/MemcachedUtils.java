package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.cache.memcached.MemcachedCacheStore;

public class MemcachedUtils {
	public static void setUp() {
	    Caches.resetMemcachedCheck();
		System.setProperty("warwick.memcached.config", "/memcached-test.properties");
	}
	
	public static void tearDown() {
	    System.clearProperty("warwick.memcached.config");
		MemcachedCacheStore.shutdownDefaultMemcachedClient();
		Caches.resetMemcachedCheck();
	}
}
