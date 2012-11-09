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
	
	V get(final K key) throws CacheEntryUpdateException;
	Map<K,V> get(final List<K> keys) throws CacheEntryUpdateException;
	
	/**
	 * Manually puts a new entry into the cache. Usually you don't
	 * need to do this as the cache is self-populating via an entry
	 * factory, but there are some use cases for manual puts (such
	 * as saving a new entry under an extra key).
	 */
	void put(final CacheEntry<K,V> entry);
	
	void addCacheListener(CacheListener<K,V> listener);
	CacheStatistics getStatistics();
	
	void setMaxSize(int size);
	void setTimeout(int seconds);
	
	void setExpiryStrategy(CacheExpiryStrategy<K,V> strategy);

	void setAsynchronousUpdateEnabled(boolean b);
	
	boolean remove(K key);
	boolean clear();
	boolean contains(K key);
	
	void shutdown();
	
}
