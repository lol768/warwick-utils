package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.cache.ehcache.EhCacheStore;

public class EhCacheUtils {
	public static void setUp() {
	    Caches.resetEhCacheCheck();
		System.setProperty("warwick.ehcache.config", "/ehcache-test-config.xml");
	}
	
	public static void tearDown() {
	    System.clearProperty("warwick.ehcache.config");
		EhCacheStore.shutdownDefaultCacheManager();
		Caches.resetEhCacheCheck();
	}
}
