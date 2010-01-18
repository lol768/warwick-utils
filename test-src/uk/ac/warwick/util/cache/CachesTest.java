package uk.ac.warwick.util.cache;

import junit.framework.TestCase;
import uk.ac.warwick.util.cache.ehcache.EhCacheStore;

public class CachesTest extends TestCase {
    
    private static final String CACHE_NAME = "customCache";
    
	@Override
	protected void setUp() throws Exception {
		EhCacheUtils.setUp();
	}	
	
	@Override
	protected void tearDown() throws Exception {
		EhCacheUtils.tearDown();
	}
	
	/**
	 * Should return false as we haven't specified a disk cache location in a system property.
	 */
	public void testGetAvailableCache() throws Exception {
		CacheStore<String,String> store = Caches.<String,String>newCacheStore(CACHE_NAME);
		assertFalse(store instanceof EhCacheStore<?,?>);
	}
}
