package uk.ac.warwick.util.cache;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BasicCacheTest {
    
    private static final String CACHE_NAME = "customCache";

	BasicCache<String, String> cache;
	BasicCache<String, String> slowCache;
	private BrokenCacheEntryFactory slowFactory;
	
	@Test
	public void getMissingValue() throws Exception {
		assertEquals("Value for dog", cache.get("dog"));
		assertEquals("Value for cat", cache.get("cat"));
		
		// getting the same key twice will return the actual same object,
		// not just equal objects
		assertSame(cache.get("frog"), cache.get("frog"));
	}
	
	@Test
	public void slowConcurrentLookups() throws Exception {
		assertFactoryCount(0);
		
		Runnable getDog = new Runnable() {
			public void run() {
				try {
					slowCache.get("dog");
				} catch (CacheEntryUpdateException e) {
					throw e.getRuntimeException();
				}
			}
		};
		
		Thread t1 = new Thread(getDog);
		Thread t2 = new Thread(getDog);
		
		t1.start();
		t2.start();
		
		// Both threads will try to get the value, because we don't
		// have write locking.
		Thread.sleep(100);
		
		// check that the lock for dog doesn't block other lookups like frog
		slowFactory.addFastRequest("frog"); //simulate a lookup that doesn't block forever
		assertEquals("Value for frog", slowCache.get("frog"));
		
		// let the dog requests go through
		slowFactory.stopBlocking();
		t1.join();
		t2.join();
		
		// check that the factory was only called once.
		List<String> requests = slowFactory.getObjectsCreated();
		assertEquals(3, requests.size());
		assertEquals("frog", requests.get(0));
		assertEquals("dog", requests.get(1));
	}
	
	@Test
	public void asynchronousUpdates() throws Exception {
		slowCache = Caches.newCache(CACHE_NAME, slowFactory, 1);
		slowCache.setAsynchronousUpdateEnabled(true);
		slowFactory.addFastRequest("one");
		
		assertFactoryCount(0);
		String result1 = slowCache.get("one");
		String result2 = slowCache.get("one");
		Thread.sleep(1100);
		assertFactoryCount(1);
		String result3 = slowCache.get("one");
		assertFactoryCount(1);
		Thread.sleep(50);
		assertFactoryCount(2);
		String result4 = slowCache.get("one");
		assertFactoryCount(2);
		
		assertSame("Should have got cached value the second time", result1, result2);
		assertSame("Should have got stale value", result2, result3);
		assertNotSame("Should have returned async-updated result", result3, result4);
		
		slowCache.shutdown();
	}
	
	private void assertFactoryCount(final int number) {
		assertEquals(number, slowFactory.getObjectsCreated().size());
	}
	
	@Test
	public void sizeRestriction() throws Exception {	
		int cacheSize = 4;
		cache.setMaxSize(cacheSize);
		
		assertEquals("Should start empty", 0, cache.getStatistics().getCacheSize());
		
		cache.get("one");
		cache.get("two");
		cache.get("three");
		cache.get("four");
		assertEquals(4, cache.getStatistics().getCacheSize());
		cache.get("five");
		cache.get("six");
		assertEquals(4, cache.getStatistics().getCacheSize());
		
		assertEquals("Shouldn't exceed maximum size", cacheSize, cache.getStatistics().getCacheSize());
		assertFalse("Oldest entry should be evicted", cache.contains("one"));
	}
	
	@Test
	public void expiry() throws Exception {
		slowCache = Caches.newCache(CACHE_NAME, slowFactory, 1);
		slowFactory.addFastRequest("one");
		
		String result1 = slowCache.get("one");
		System.err.println("Got first item");
		String result2 = slowCache.get("one");
		Thread.sleep(1100);
		String result3 = slowCache.get("one");
		
		assertSame(result1, result2);
		assertNotSame(result1, result3);
		
		slowCache.shutdown();
	}
	
	@Test
	public void concurrentInitialRequests() throws Exception {
		// UTL-132: This is a bit of a strange test because we're replicating very
		// specific conditions - we're simulating a condition where another
		// thread has updated an item in the store in-between two calls. We
		// aren't actually using real concurrency to replicate that condition,
		// because it's quite hard to do, so we're just replicating the specific
		// error condition.
		
		// This CacheStore _always_ returns null the first time, then returns the value the second time
		final CacheStore<String, String> store = new CacheStore<String, String>() {
			private final Set<String> called = new HashSet<String>(); 
			
			public CacheEntry<String, String> get(String key) {
				if (called.contains(key)) {
					return new CacheEntry<String, String>(key, "Value for " + key);
				} else {
					called.add(key);
					return null;
				}
			}

			public void put(CacheEntry<String, String> entry) {
				called.add(entry.getKey());
			}

			public boolean remove(String key) {
				throw new UnsupportedOperationException();
			}

			public CacheStatistics getStatistics() {
				throw new UnsupportedOperationException();
			}

			public void setMaxSize(int max) {
				throw new UnsupportedOperationException();
			}

			public boolean clear() {
				throw new UnsupportedOperationException();
			}

			public boolean contains(String key) {
				throw new UnsupportedOperationException();
			}

			public void shutdown() {
				throw new UnsupportedOperationException();
			}
		};	
		
		BasicCache<String, String> cache = new BasicCache<String, String>(store, new SingularCacheEntryFactory<String, String>() {
			public String create(String key) {
				return new String("Value for " + key);
			}
			public boolean shouldBeCached(String val) {
				return true;
			}
		}, 100);
		
		assertEquals("Value for steve", cache.get("steve"));
	}
	
	@Before
	public void setUp() throws Exception {
		EhCacheUtils.setUp();
		cache = Caches.newCache(CACHE_NAME, new SingularCacheEntryFactory<String, String>() {
			public String create(String key) {
				return new String("Value for " + key);
			}
			public boolean shouldBeCached(String val) {
				return true;
			}
		}, 100);
		
		slowFactory = new BrokenCacheEntryFactory();
		slowCache = Caches.newCache(CACHE_NAME, slowFactory, 100);
	}
	
	@After
	public void tearDown() throws Exception {
		System.out.println("Tearing down");
		cache.shutdown();
		EhCacheUtils.tearDown();
	}

	/**
	 * Mock entry factory that by default will block on a lookup until
	 * stopBlocking() is called. If you add some entries with {@link #addFastRequest(String)}
	 * then these will always return immediately, so you can test lookups
	 * while others are still processing.
	 */
	class BrokenCacheEntryFactory implements CacheEntryFactory<String, String> {
		private volatile boolean blocking = true;
		
		private List<String> requests = Collections.synchronizedList(new ArrayList<String>());
		
		// if a key is in here it'll return straight away.
		private Set<String> fastRequests = new HashSet<String>();
		
		public synchronized String create(String key) {
			if (!fastRequests.contains(key)) {
				while (blocking) {
					try {
						wait();
					} catch (InterruptedException e) {
						//keep on waitin'
					}
				}
			}
			requests.add(key);
			return new String("Value for " + key);
		}
		
		public synchronized void stopBlocking() {
			blocking = false;
			notifyAll();
		}

		public List<String> getObjectsCreated() {
			return requests;
		}
		
		public void addFastRequest(String s) {
			fastRequests.add(s);
		}

		/**
		 * Most implementations wouldn't bother implementing this if it
		 * was just going to look up each individually. Pretend as though
		 * this were a batch lookup.
		 */
		public Map<String, String> create(List<String> keys)
				throws CacheEntryUpdateException {
			Map<String,String> response = new HashMap<String, String>();
			for (String key : keys) {
				response.put(key, create(key));
			}
			return response;
		}

		public boolean isSupportsMultiLookups() {
			return true;
		}

		public boolean shouldBeCached(String val) {
			return true;
		}
	}

}
