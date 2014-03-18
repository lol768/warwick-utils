package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * Cache interface for a self-populating cache. 
 * 
 * K - the key type
 * V - the value type
 */
public interface Cache<K extends Serializable,V extends Serializable> {

	static interface Result<V> {
		/**
		 * @return Is the cache currently updating this key in the background?
		 */
		boolean isUpdating();

		/**
		 * @return When the cache entry was last updated, or -1 if no cache entry.
		 */
		long getLastUpdated();

		/**
		 * @return The cached value. May be null if cache is configured to only update asynchronously.
		 */
		V getValue();
	}

	static class ResultImpl<V> implements Result<V> {
		private final V value;
		private final boolean updating;
		private final long lastUpdated;
		public ResultImpl(V value, boolean updating, long lastUpdated) {
			this.value = value;
			this.updating = updating;
			this.lastUpdated = lastUpdated;
		}
		public long getLastUpdated() {
			return lastUpdated;
		}
		public V getValue() {
			return value;
		}
		public boolean isUpdating() {
			return updating;
		}
	}

	/**
	 * If the cache is asynchronous-only, you should use getResult
	 * instead as it gives more information.
	 */
	V get(final K key) throws CacheEntryUpdateException;
	Map<K,V> get(final List<K> keys) throws CacheEntryUpdateException;

	/**
	 * Gets the value for this key, wrapped in a Result which provides some
	 * metadata about the returned value (which may be empty).
	 */
	Result<V> getResult(final K key) throws CacheEntryUpdateException;
//	Map<K,Result<V>> getResults(final List<K> keys) throws CacheEntryUpdateException;

	/**
	 * Manually puts a new entry into the cache. Usually you don't
	 * need to do this as the cache is self-populating via an entry
	 * factory, but there are some use cases for manual puts (such
	 * as saving a new entry under an extra key).
	 */
	void put(final CacheEntry<K,V> entry);
	
	void addCacheListener(CacheListener<K,V> listener);
	CacheStatistics getStatistics() throws CacheStoreUnavailableException;
	
	void setMaxSize(int size);
	void setTimeout(int seconds);
	
	void setExpiryStrategy(CacheExpiryStrategy<K,V> strategy);

	void setAsynchronousUpdateEnabled(boolean b);

    String getName();
	
	boolean remove(K key);
	boolean clear();
	boolean contains(K key);
	
	void shutdown();
	
}
