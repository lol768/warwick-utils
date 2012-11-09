package uk.ac.warwick.util.cache;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

public class HashMapCacheStoreTest extends TestCase {
	
	/**
	 * Test the behaviour of creating multiple HashMapCacheStores with the same
	 * name - they will use the same map internally, but can be of different
	 * types. Trying to get a value of a different type will cause a ClassCastException
	 * at runtime. 
	 */
	public void testMultipleStores() {
		HashMapCacheStore<String, String> cache = new HashMapCacheStore<String, String>("MyCache");
		HashMapCacheStore<String, String> cache2 = new HashMapCacheStore<String, String>("MyCache");
		HashMapCacheStore<String, ArrayList<?>> cache3 = new HashMapCacheStore<String, ArrayList<?>>("MyCache");
		
		cache.put(new CacheEntry<String, String>("one", "gamma"));
		
		assertEquals("gamma", cache2.get("one").getValue());
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList("blah","blah"));
		cache3.put(new CacheEntry<String, ArrayList<?>>("two", list));
		
		assertEquals(list, cache3.get("two").getValue());
		
		try {
			@SuppressWarnings("unused")
            ArrayList<?> l = cache3.get("one").getValue();
			fail("should have failed with incompatible value");
		} catch (ClassCastException e) {}
		
		try {
            @SuppressWarnings("unused")
			String s = cache2.get("two").getValue();
			fail("should have failed with incompatible value");
		} catch (ClassCastException e) {}
		
	}
}
