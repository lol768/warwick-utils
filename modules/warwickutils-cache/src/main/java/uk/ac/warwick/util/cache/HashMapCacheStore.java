package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache store that uses a LinkedHashMap to evict the oldest items.
 * 
 * If you create two stores with the same name, they will use the same backing Map.
 * This normally doesn't cause a problem but you will probably find that only the
 * first created HashMapCacheStore will be able to set the size of the cache.
 * 
 * The Map is not typed simply because sometimes it's useful to have multiple
 * caches of different types sharing a cache. 
 * 
 * @author cusebr
 */
public final class HashMapCacheStore<K extends Serializable,V extends Serializable> implements CacheStore<K, V> {

	private static final int DEFAULT_CACHE_SIZE = 10_000;

	private static ConcurrentMap<String, WeakReference<Map<?, ?>>> maps = new ConcurrentHashMap<>();

	public static class Builder<K extends Serializable, V extends Serializable, T> implements Caches.Builder<K, V, T> {
		private final String name;
		private CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory;
		private CacheExpiryStrategy<K, V> expiryStrategy = TTLCacheExpiryStrategy.eternal();
		private long maximumSize = DEFAULT_CACHE_SIZE;
		private boolean asynchronousUpdateEnabled;
		private boolean asynchronousOnly;

		public Builder(String name, CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory) {
			this.name = name;
			this.entryFactory = entryFactory;
		}

		private Builder(String name, CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory, CacheExpiryStrategy<K, V> expiryStrategy, long maximumSize, boolean asynchronousUpdateEnabled, boolean asynchronousOnly) {
			this(name, entryFactory);
			this.expiryStrategy = expiryStrategy;
			this.maximumSize = maximumSize;
			this.asynchronousUpdateEnabled = asynchronousUpdateEnabled;
			this.asynchronousOnly = asynchronousOnly;
		}

		@Override
		public <U> Builder<K, V, U> dataInitialisingEntryFactory(CacheEntryFactoryWithDataInitialisation<K, V, U> entryFactory) {
			return new Builder<>(name, entryFactory, expiryStrategy, maximumSize, asynchronousUpdateEnabled, asynchronousOnly);
		}

		@Override
		public Builder<K, V, T> expireAfterWrite(Duration duration) {
			this.expiryStrategy = TTLCacheExpiryStrategy.forTTL(duration);
			return this;
		}

		@Override
		public Caches.Builder<K, V, T> expiryStategy(CacheExpiryStrategy<K, V> expiryStrategy) {
			this.expiryStrategy = expiryStrategy;
			return this;
		}

		@Override
		public Builder<K, V, T> maximumSize(long size) {
			this.maximumSize = size;
			return this;
		}

		@Override
		public Caches.Builder<K, V, T> asynchronous() {
			this.asynchronousUpdateEnabled = true;
			return this;
		}

		@Override
		public Caches.Builder<K, V, T> asynchronousOnly() {
			this.asynchronousUpdateEnabled = true;
			this.asynchronousOnly = true;
			return this;
		}

		@Override
		public Builder<K, V, T> properties(Properties properties) {
			throw new UnsupportedOperationException("Properties can only be set with Memcached cache stores");
		}

		@Override
		@SuppressWarnings("unchecked")
		public HashMapCacheStore<K, V> buildStore() {
			return new HashMapCacheStore<>(name, (Map<K, CacheEntry<K, V>>) maps.computeIfAbsent(name, n ->
				new WeakReference<>(Collections.synchronizedMap(new LinkedHashMap<K, CacheEntry<K, V>>() {
					private static final long serialVersionUID = 1L;
					protected boolean removeEldestEntry(final Map.Entry<K, CacheEntry<K, V>> eldest) {
						return size() > maximumSize;
					}
				}))
			).get());
		}

		@Override
		public CacheWithDataInitialisation<K, V, T> build() {
			return new BasicCache<>(buildStore(), entryFactory, expiryStrategy, asynchronousUpdateEnabled, asynchronousOnly);
		}
	}
	
	/**
	 * Synchronized means we're pretty thread safe but you still need to manually synchronize over
	 * loops. We don't do any loops at the moment.
	 */
	private final Map<K, CacheEntry<K, V>> map;
	private final String name;
	
	private HashMapCacheStore(String name, Map<K, CacheEntry<K, V>> map) {
		this.name = name;
		this.map = map;
	}
	
	/**
	 * If you try to get a key for a value that is of a different type,
	 * you will get a ClassCastException when retrieving the value.
	 */
	public CacheEntry<K, V> get(K key) {
		return map.get(key);
	}

	public void put(CacheEntry<K, V> entry, Duration ttl) {
		map.put(entry.getKey(), entry);
	}

	public boolean remove(K key) {
		map.remove(key);
		return true;
	}

	public CacheStatistics getStatistics() {
		return new CacheStatistics(
			map.size()	
		);
	}

	public void setMaxSize(int max) {
		// Ignored
	}

	public boolean clear() {
		map.clear();
		return true;
	}

	public boolean contains(K key) {
		return map.containsKey(key);
	}

	public void shutdown() {
		clear();
		maps.remove(name);
	}

    public String getName() {
        return name;
    }

    /** Empty all hashmap cache stores. For debugging. */
    public static void clearAll() {
        for (WeakReference<Map<?, ?>> ref : maps.values()) {
            Map<?, ?> m = ref.get();
            if (m != null) {
                m.clear();
            }
        }
    }

}
