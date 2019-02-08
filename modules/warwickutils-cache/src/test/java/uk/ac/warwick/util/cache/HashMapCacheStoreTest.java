package uk.ac.warwick.util.cache;

import junit.framework.TestCase;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class HashMapCacheStoreTest extends TestCase {
	
	/**
	 * Test the behaviour of creating multiple HashMapCacheStores with the same
	 * name - they will use the same map internally, but can be of different
	 * types. Trying to get a value of a different type will cause a ClassCastException
	 * at runtime. 
	 */
	public void testMultipleStores() {
		HashMapCacheStore<String, String> cache = new HashMapCacheStore.Builder<String, String, Object>("MyCache", null).buildStore();
		HashMapCacheStore<String, String> cache2 = new HashMapCacheStore.Builder<String, String, Object>("MyCache", null).buildStore();
		HashMapCacheStore<String, ArrayList<?>> cache3 = new HashMapCacheStore.Builder<String, ArrayList<?>, Object>("MyCache", null).buildStore();
		
		cache.put(new CacheEntry<>("one", "gamma"), Duration.ofSeconds(10));
		
		assertEquals("gamma", cache2.get("one").getValue());
		
		ArrayList<String> list = new ArrayList<>(Arrays.asList("blah","blah"));
		cache3.put(new CacheEntry<>("two", list), Duration.ofSeconds(10));
		
		assertEquals(list, cache3.get("two").getValue());
		
		try {
			@SuppressWarnings("unused")
            ArrayList<?> l = cache3.get("one").getValue();
			fail("should have failed with incompatible value");
		} catch (ClassCastException e) {
			// This is fine
		}
		
		try {
            @SuppressWarnings("unused")
			String s = cache2.get("two").getValue();
			fail("should have failed with incompatible value");
		} catch (ClassCastException e) {
			// This is fine
		}
		
	}
}
