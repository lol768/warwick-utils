package uk.ac.warwick.util.cache;

import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Cache which implements the following features
 * 
 * <ul>
 *  <li>Self-population (updates are done during get() via the EntryFactory)</li>
 *  <li>Asynchronous background updates for expired entries</li>
 * </ul>
 * <p>
 * The backing CacheStore determines how elements are evicted when we grow
 * past the maximum cache size.
 */
public final class BasicCache<K extends Serializable, V extends Serializable, T> implements CacheWithDataInitialisation<K, V, T> {

	private final CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory;
	
	private final List<CacheListener<K, V>> listeners = new ArrayList<>();
	
	// Uses an unbounded queue, so when all threads are busy it will queue up
	// waiting jobs and do them next
	private static ExecutorService staticThreadPool = new ThreadPoolExecutor(1, 16, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

	private ExecutorService threadPool = staticThreadPool;

	private final CacheStore<K, V> store;

	private final CacheExpiryStrategy<K, V> expiryStrategy;
	
	private final boolean asynchronousUpdateEnabled;

	/**
	 * If asynchronousUpdateEnabled is also enabled, the cache will _only_
	 * do asynchronous updates, returning no value if there is none in the
	 * cache.
	 *
	 * If asynchronousUpdateEnabled is false, this has no effect.
	 */
	private final boolean asynchronousOnly;

	public BasicCache(@Nonnull CacheStore<K, V> cacheStore, @Nullable CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory, @Nonnull CacheExpiryStrategy<K, V> expiryStrategy, boolean asynchronousUpdateEnabled, boolean asynchronousOnly) {
		if (asynchronousOnly && !asynchronousUpdateEnabled) {
			throw new IllegalArgumentException("Can't have asynchronousOnly if no asynchronousUpdateEnabled");
		}

		this.entryFactory = entryFactory;
		this.expiryStrategy = expiryStrategy;
		this.store = cacheStore;
		this.asynchronousUpdateEnabled = asynchronousUpdateEnabled;
		this.asynchronousOnly = asynchronousOnly;
	}

	/**
	 * Gets the value for the given key. A read lock is initially applied.
	 * If the cache value is present and valid then we unlock and return the
	 * value. If it's not present or it's expired, it will get a new value from
	 * the EntryFactory and place it in, and that is returned.
	 * <p>
	 * 
	 */
	@Override
	public V get(final K key) throws CacheEntryUpdateException {
        return get(key, null);
	}

	@Override
    public V get(final K key, final T data) throws CacheEntryUpdateException {
        return getResult(key, data).getValue();
    }
	
	/**
	 * If the EntryFactory supports it, looks up a collection of keys in one go.
	 * 
	 * First it finds which of the keys are currently in the cache and valid. It
	 * gathers up which ones are missing and which are expired. If none are missing
	 * but some are expired, the stale values are used and updates run asynchronously
	 * (when asynchronous updates are enabled).
	 * 
	 * If any entries are missing then ALL lookups are done syncronously, including
	 * any expired entries. If the client has to wait for one lookup, we may as well
	 * do it all now and get fresh data for every key.
	 */
	@Override
	public Map<K, V> get(List<K> keys) throws CacheEntryUpdateException {
		if (entryFactory == null) {
			throw new UnsupportedOperationException("Batch lookups only supported when a suitable EntryFactory is used");
		}

        if (!entryFactory.isSupportsMultiLookups()) {
			throw new UnsupportedOperationException("The given EntryFactory does not support batch lookups");
		}

		if (asynchronousOnly) {
			throw new UnsupportedOperationException("Multiple lookups not yet implemented for asynchronous-only caches.");
		}
		
		Map<K, V> results = new HashMap<>();
		List<KeyEntry<K, V, T>> missing = new ArrayList<>();
		List<KeyEntry<K, V, T>> expired = new ArrayList<>();
		for (K key : keys) {
            try {
                CacheEntry<K, V> entry = store.get(key);
                if (entry == null || (!asynchronousUpdateEnabled && isExpired(entry))) {
                    missing.add(new KeyEntry<>(key, entry, null));
                } else {
                    results.put(key, entry.getValue());
                    if (isStale(entry)) {
                        expired.add(new KeyEntry<>(key, entry, null));
                    }
                }
            } catch (CacheStoreUnavailableException e) {
                missing.add(new KeyEntry<>(key, null, null));
            }
		}
		
		if (!missing.isEmpty()) {
			missing.addAll(expired);
			Map<K, CacheEntry<K, V>> updated = updateEntries(missing);
			for (Map.Entry<K,CacheEntry<K, V>> entry : updated.entrySet()) {
				results.put(entry.getKey(), entry.getValue().getValue());
			}
		} else if (!expired.isEmpty()) {
			threadPool.execute(UpdateCacheEntryTask.task(this, expired));
		}
		
		return results;
	}

    public CacheEntry<K, V> getOrNull(K key) {
        try {
            return store.get(key);
        } catch (CacheStoreUnavailableException e) {
            return null;
        }
    }

    @Override
    public Result<V> getResult(K key) throws CacheEntryUpdateException {
        return getResult(key, null);
    }

	@Override
	public Result<V> getResult(K key, T data) throws CacheEntryUpdateException {
		CacheEntry<K, V> entry = getOrNull(key);

		boolean expired = ( entry != null && isExpired(entry) );
        boolean stale = ( entry != null && isStale(entry) );

		boolean updating = false;
		if (entry != null && !expired) {
			broadcastHit(key, entry);

            if (stale && !asynchronousOnly) {
                // Entry is just expired. Return the stale version
                // now and update in the background
                threadPool.execute(UpdateCacheEntryTask.task(this, new KeyEntry<>(key, entry, data)));
                updating = true;
            }
		} else if (entryFactory != null) {
			if (!asynchronousOnly && (entry == null || !asynchronousUpdateEnabled)) {
				entry = updateEntry(new KeyEntry<>(key, entry, data));
			} else {
				// Entry is just expired. Return the stale version
				// now and update in the background
				threadPool.execute(UpdateCacheEntryTask.task(this, new KeyEntry<>(key, entry, data)));
				updating = true;
			}
		}

		V value = null;
		long lastUpdated = -1;
		if (entry != null) {
			value = entry.getValue();
			lastUpdated = entry.getTimestamp();
			// either we started an update task just now, or an existing task is already updating.
			updating = updating || entry.isUpdating();
		}
		return new ResultImpl<>(value, updating, lastUpdated);
	}

	private void broadcastMiss(final K key, final CacheEntry<K, V> newEntry) {
		for (CacheListener<K, V> listener : listeners) {
			listener.cacheMiss(key, newEntry);
		}
	}

	private void broadcastHit(final K key, final CacheEntry<K, V> entry) {
		for (CacheListener<K, V> listener : listeners) {
			listener.cacheHit(key, entry);
		}
	}

	/**
	 * Updates the given key with a value from the factory and places it in the cache.
	 * 
	 * @param kEntry The key and entry
	 */
	CacheEntry<K, V> updateEntry(final KeyEntry<K, V, T> kEntry) throws CacheEntryUpdateException {
		final K key = kEntry.key;
        final T data = kEntry.data;
		final CacheEntry<K, V> foundEntry = kEntry.entry;

        CacheEntry<K, V> entry = getOrNull(key);
		
		// if entry is null, we definitely need to go update it.
		// if entry is not currently updating, update it UNLESS the version we
		// got outside the lock is a different object - meaning another thread just updated it
		if (entry == null || (entry.equals(foundEntry) && !entry.isUpdating())) {
			if (entry != null) {
				entry.setUpdating(true);
			}
			try {
				V newValue = entryFactory.create(key, data);
				entry = newEntry(key, newValue);
				if (entryFactory.shouldBeCached(newValue)) {
                    put(entry);
				}
				broadcastMiss(key, entry);
			} finally {
				if (entry != null) {
					entry.setUpdating(false);
				}
			}
		} else {
			// entry in map changed since we got the write lock, meaning another thread
			// just replaced it - so we've essentially hit the cache and can return
			// this value.
			broadcastHit(key, entry);
		}
		
		return entry;
	}
	
	/**
	 * Updates all the given key/value pairs and puts them in the map.
	 * 
	 * Doesn't obtain a write lock, because this is too complicated. This doesn't
	 * affect thread-safety but it can result in multiple threads updating the
	 * same cache keys.
	 */
	Map<K, CacheEntry<K, V>> updateEntries(final Collection<KeyEntry<K, V, T>> kentries) throws CacheEntryUpdateException {
		Map<K, CacheEntry<K, V>> result = new HashMap<>();
		List<K> keys = new ArrayList<>();
		for (KeyEntry<K, V, T> kentry : kentries) {
			final CacheEntry<K, V> foundEntry = kentry.entry;
			if (foundEntry != null) {
				foundEntry.setUpdating(true);
			}
			keys.add(kentry.key);
		}
		
		Map<K, V> createdMap = entryFactory.create(keys);
		for (Map.Entry<K, V> created : createdMap.entrySet()) {
			final K key = created.getKey();
			final V value = created.getValue();
			final CacheEntry<K, V> entry = new CacheEntry<>(key, value);
			if (entryFactory.shouldBeCached(value)) {
                put(entry);
			}
			result.put(key, entry);
			broadcastMiss(key, entry);
		}
		return result;
	}

	public void put(CacheEntry<K, V> entry) {
        try {
            store.put(entry, expiryStrategy.getTTL(entry));
        } catch (CacheStoreUnavailableException e) {
            // do nothing
        }
	}

	public boolean remove(final K key) {
        try {
            return store.remove(key);
        } catch (CacheStoreUnavailableException e) {
            return false;
        }
	}
	
	private CacheEntry<K, V> newEntry(K key, V newValue) {
		return new CacheEntry<>(key, newValue);
	}
	
	private boolean isExpired(final CacheEntry<K, V> entry) {
		return expiryStrategy.isExpired(entry);
	}

    private boolean isStale(final CacheEntry<K, V> entry) {
        return expiryStrategy.isStale(entry);
    }

	public void addCacheListener(CacheListener<K, V> listener) {
		listeners.add(listener);
	}

	public CacheStatistics getStatistics() throws CacheStoreUnavailableException {
        return store.getStatistics();
	}

	public boolean isAsynchronousUpdateEnabled() {
		return asynchronousUpdateEnabled;
	}

	public boolean isAsynchronousOnly() {
		return asynchronousOnly;
	}

	public boolean clear() {
        try {
            return store.clear();
        } catch (CacheStoreUnavailableException e) {
            return false;
        }
	}

	public boolean contains(K key) {
        try {
            return store.contains(key);
        } catch (CacheStoreUnavailableException e) {
            return false;
        }
	}

    public String getName() {
        return this.store.getName();
    }

	/**
	 * Holds a key and an entry. Even though Entry has getKey(), entry can
	 * be null so we still need this internally.
	 */
	static class KeyEntry<K extends Serializable,V extends Serializable,T> {
		public final K key;
        public final T data;
		public final CacheEntry<K, V> entry;
		public KeyEntry(K k, CacheEntry<K, V> e, T d) {
			key = k;
			entry = e;
            data = d;
		}
	}

	public void shutdown() {
		store.shutdown();
	}

	/**
	 * Replace the static thread pool with a new one, if you have special requirements
	 * for the number of threads or whatnot. Signals to the current threadpool to shutdown.
	 */
	public static void setThreadPool(ExecutorService threadPool) {
		if (BasicCache.staticThreadPool != null) {
			BasicCache.staticThreadPool.shutdown();
		}
		BasicCache.staticThreadPool = threadPool;
	}

	/** Alternative to the static method which is shared across all BasicCaches,
	 * this just sets it for this cache.
	 */
	public final void setLocalThreadPool(ExecutorService newThreadPool) {
		this.threadPool = newThreadPool;
	}

	@VisibleForTesting
	CacheStore<K, V> getCacheStore() {
		return store;
	}
}
