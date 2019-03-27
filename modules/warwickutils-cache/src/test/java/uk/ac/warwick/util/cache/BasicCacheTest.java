package uk.ac.warwick.util.cache;

import org.jmock.lib.concurrent.DeterministicScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.cache.caffeine.CaffeineCacheStore;

import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;

public class BasicCacheTest {
    
    private static final String CACHE_NAME = "customCache";
	private static final String SLOW_CACHE_NAME = "slowCustomCache";
	private static final String NO_FACTORY_CACHE_NAME = "noFactoryCustomCache";

	private Cache<String, String> cache;
	private Cache<String, String> slowCache;
	private BrokenCacheEntryFactory slowFactory;

    private Cache<String, String> noFactoryCache;

	// BasicCache minimum expiry is 1 second, this makes it 100ms to minimise sleep times.
	private final CacheExpiryStrategy<String, String> shortExpiry = TTLCacheExpiryStrategy.forTTL(Duration.ofMillis(100));

	@Before
	public void setUp() {
		cache = Caches.builder(CACHE_NAME, new SingularCacheEntryFactory<String, String>() {
				public String create(String key) {
					return "Value for " + key;
				}
				public boolean shouldBeCached(String val) {
				return true;
			}
			})
			.expireAfterWrite(Duration.ofSeconds(100))
			.maximumSize(100)
			.build();

		slowFactory = new BrokenCacheEntryFactory();
		slowCache = Caches.builder(SLOW_CACHE_NAME, slowFactory)
			.expireAfterWrite(Duration.ofSeconds(100))
			.maximumSize(100)
			.build();

		noFactoryCache = Caches.<String, String>builder(NO_FACTORY_CACHE_NAME)
			.expireAfterWrite(Duration.ofSeconds(100))
			.maximumSize(100)
			.build();
	}

	@After
	public void tearDown() {
		cache.shutdown();
		slowCache.shutdown();
		noFactoryCache.shutdown();
	}
	
	@Test
	public void getMissingValue() throws Exception {
		assertEquals("Value for dog", cache.get("dog"));
		assertEquals("Value for cat", cache.get("cat"));
		
		// getting the same key twice will return the actual same object,
		// not just equal objects
		assertSame(cache.get("frog"), cache.get("frog"));
	}

    @Test
    public void noFactory() throws Exception {
        noFactoryCache.put(new CacheEntry<>("cat", "meow"));
        assertNull(noFactoryCache.get("dog"));
        assertEquals("meow", noFactoryCache.get("cat"));
    }

	@Test
	public void multiLookupsSynchronous() throws Exception {
		slowFactory.stopBlocking();
		Map<String, String> expected = new HashMap<>();
		expected.put("dog", "Value for dog");
		expected.put("cat", "Value for cat");
		assertEquals(expected, slowCache.get(Arrays.asList("dog", "cat")));
	}

	// Asynchronous-only batch cache requests are not (yet) implemented.
	@Test(expected=UnsupportedOperationException.class)
	public void multiLookupsAsynchronousOnly() throws Exception {
		slowFactory.stopBlocking();

		slowCache = Caches.builder("customSlowCache1", slowFactory)
			.expireAfterWrite(Duration.ofSeconds(100))
			.asynchronousOnly()
			.maximumSize(100)
			.build();

		slowCache.get(Arrays.asList("dog", "cat"));
	}
	
	@Test
	public void slowConcurrentLookups() throws Exception {
		assertFactoryCount(0);
		
		Runnable getDog = () -> {
			try {
				slowCache.get("dog");
			} catch (CacheEntryUpdateException e) {
				throw e.getRuntimeException();
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
		slowCache = Caches.builder("asynchronousUpdates", slowFactory)
			.expiryStategy(shortExpiry)
			.asynchronous()
			.maximumSize(100)
			.build();

		slowFactory.addFastRequest("one");
		
		assertFactoryCount(0);
		String result1 = slowCache.get("one");
		String result2 = slowCache.get("one");
		Thread.sleep(150);
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

		cache = Caches.builder("sizeRestriction", new SingularCacheEntryFactory<String, String>() {
				public String create(String key) {
					return "Value for " + key;
				}
				public boolean shouldBeCached(String val) {
					return true;
				}
			})
			.expireAfterWrite(Duration.ofSeconds(100))
			.maximumSize(cacheSize)
			.build();
		
		assertEquals("Should start empty", 0, cache.getStatistics().getCacheSize());
		
		cache.get("one");
		cache.get("two");
		cache.get("three");
		cache.get("four");
		assertEquals(4, cache.getStatistics().getCacheSize());
		cache.get("five");
		cache.get("six");
		((CaffeineCacheStore<String, String>) ((BasicCache<String, String, Object>) cache).getCacheStore()).getCaffeineCache().cleanUp();
		assertEquals(4, cache.getStatistics().getCacheSize());
		
		assertEquals("Shouldn't exceed maximum size", cacheSize, cache.getStatistics().getCacheSize());
		assertFalse("Oldest entry should be evicted", cache.contains("one"));
	}
	
	@Test
	public void expiry() throws Exception {
		slowCache = Caches.builder("expiry", slowFactory)
			.expiryStategy(shortExpiry)
			.maximumSize(100)
			.build();

		slowFactory.addFastRequest("one");
		
		String result1 = slowCache.get("one");
		System.err.println("Got first item");
		String result2 = slowCache.get("one");
		Thread.sleep(150);
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
			private final Set<String> called = new HashSet<>();
			
			public CacheEntry<String, String> get(String key) {
				if (called.contains(key)) {
					return new CacheEntry<>(key, "Value for " + key);
				} else {
					called.add(key);
					return null;
				}
			}

			public void put(CacheEntry<String, String> entry, Duration ttl) {
				called.add(entry.getKey());
			}

			public boolean remove(String key) {
				throw new UnsupportedOperationException();
			}

			public CacheStatistics getStatistics() {
				throw new UnsupportedOperationException();
			}

			public void setMaxSize(int size) {
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

            public String getName() {
                throw new UnsupportedOperationException();
            }
		};	
		
		BasicCache<String, String, Object> cache = new BasicCache<>(store, Caches.wrapFactoryWithoutDataInitialisation(new SingularCacheEntryFactory<String, String>() {
			public String create(String key) {
				return "Value for " + key;
			}
			public boolean shouldBeCached(String val) {
				return true;
			}
		}), TTLCacheExpiryStrategy.forTTL(Duration.ofSeconds(100)), false, false);
		
		assertEquals("Value for steve", cache.get("steve"));
	}

	@Test
	public void asynchronousOnlyGetReturnsNull() throws Exception {
		DeterministicScheduler scheduler = new DeterministicScheduler();

		BasicCache<String, String, Object> cache =
			new BasicCache<>(
				Caches.<String, String>builder(CACHE_NAME)
					.expireAfterWrite(Duration.ofSeconds(100))
					.maximumSize(100)
					.buildStore(),
				Caches.wrapFactoryWithoutDataInitialisation(new SingularCacheEntryFactory<String, String>() {
					public String create(String key) {
						return "Value for " + key;
					}
					public boolean shouldBeCached(String val) {
						return true;
					}
				}),
				TTLCacheExpiryStrategy.forTTL(Duration.ofSeconds(100)), true, true);

		cache.setLocalThreadPool(scheduler);

		assertNull("Should be null", cache.get("alan"));
		scheduler.runUntilIdle();
		assertEquals("Should be set", "Value for alan", cache.get("alan"));
	}

	@Test
	public void asynchronousOnlyGetResultReturnsNull() throws Exception {
		DeterministicScheduler scheduler = new DeterministicScheduler();

		BasicCache<String, String, Object> cache =
				new BasicCache<>(
						Caches.<String, String>builder(CACHE_NAME)
								.expireAfterWrite(Duration.ofSeconds(100))
								.maximumSize(100)
								.buildStore(),
						Caches.wrapFactoryWithoutDataInitialisation(new SingularCacheEntryFactory<String, String>() {
							public String create(String key) {
								return "Value for " + key;
							}
							public boolean shouldBeCached(String val) {
								return true;
							}
						}),
						TTLCacheExpiryStrategy.forTTL(Duration.ofSeconds(100)), true, true);

		cache.setLocalThreadPool(scheduler);

		Cache.Result<String> result = cache.getResult("alan");
		assertEquals(-1, result.getLastUpdated());
		assertTrue("Should be updating", result.isUpdating());
		assertNull("Should be null", result.getValue());

		scheduler.runUntilIdle();

		result = cache.getResult("alan");
		assertTrue("Should have recent timestamp", result.getLastUpdated() + 1000 > System.currentTimeMillis());
		assertFalse("Should not be updating", result.isUpdating());
		assertEquals("Value for alan", result.getValue());
	}

	/**
	 * Mock entry factory that by default will block on a lookup until
	 * stopBlocking() is called. If you add some entries with {@link #addFastRequest(String)}
	 * then these will always return immediately, so you can test lookups
	 * while others are still processing.
	 */
	class BrokenCacheEntryFactory implements CacheEntryFactory<String, String> {
		private volatile boolean blocking = true;
		
		private List<String> requests = Collections.synchronizedList(new ArrayList<>());
		
		// if a key is in here it'll return straight away.
		private Set<String> fastRequests = new HashSet<>();
		
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
			return "Value for " + key;
		}
		
		synchronized void stopBlocking() {
			blocking = false;
			notifyAll();
		}

		List<String> getObjectsCreated() {
			return requests;
		}
		
		void addFastRequest(String s) {
			fastRequests.add(s);
		}

		/**
		 * Most implementations wouldn't bother implementing this if it
		 * was just going to look up each individually. Pretend as though
		 * this were a batch lookup.
		 */
		public Map<String, String> create(List<String> keys) {
			Map<String,String> response = new HashMap<>();
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
