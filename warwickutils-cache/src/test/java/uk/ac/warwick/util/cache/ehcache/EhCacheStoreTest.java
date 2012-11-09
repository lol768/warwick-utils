package uk.ac.warwick.util.cache.ehcache;

import junit.framework.TestCase;
import uk.ac.warwick.util.cache.CacheEntry;
import uk.ac.warwick.util.cache.EhCacheUtils;

public class EhCacheStoreTest extends TestCase {
    
    private static final String CACHE_NAME = "customCache";
    
	private EhCacheStore<String, String> cache;

	public void setUp() {
		// Specify an alternate Ehcache configuration for the test.
		EhCacheUtils.setUp();
		cache = new EhCacheStore<String, String>(CACHE_NAME);
	}
	
	public void tearDown() {
		cache.shutdown();
		EhCacheUtils.tearDown();
	}
	
	public void testCreation() {
		CacheEntry<String, String> string = cache.get("token:abcdefghij");
		assertNull(string);
		
		CacheEntry<String, String> entry = new CacheEntry<String, String>("token:12345", "Johnny");
		cache.put(entry);
		
		assertSame(entry, cache.get("token:12345"));
		assertSame(entry, cache.get("token:12345"));
		assertSize(1);
		
		cache.clear();
		assertSize(0);
	}
	
	public void testMaxSize() {
		//cache.setMaxSize(2); // this doesn't work - use ehcache config 
		
		assertSize(0);
		cache.put(new CacheEntry<String,String>("token:1", "one"));
		assertSize(1);
		cache.put(new CacheEntry<String,String>("token:2", "two"));
		assertSize(2);
		
		cache.put(new CacheEntry<String,String>("token:3", "three"));
		cache.put(new CacheEntry<String,String>("token:4", "three"));
		cache.put(new CacheEntry<String,String>("token:5", "three"));
		cache.put(new CacheEntry<String,String>("token:6", "three"));
		cache.flush();
		assertSize(4);
	}
	
	private void assertSize(int size) {
		assertEquals(size, cache.getStatistics().getCacheSize());
	}
}
