package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.cache.ehcache.EhCacheStore;

public class EhCacheUtils {
	public static void setUp() {
		System.setProperty("warwick.ehcache.config", "/ehcache-test-config.xml");
	}
	
	public static void tearDown() {
		EhCacheStore.shutdownDefaultCacheManager();
	}
}
