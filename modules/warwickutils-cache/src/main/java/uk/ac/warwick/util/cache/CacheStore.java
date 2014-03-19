package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * {@link CacheStore} implements a simple store of cache elements - 
 * it doesn't do anything clever with them.  
 * 
 * Implementations must be thread safe, in that concurrent calls mustn't
 * result in corrupt data or infinite loops. 
 */
public interface CacheStore<K extends Serializable,V extends Serializable> {
	CacheEntry<K,V> get(K key) throws CacheStoreUnavailableException;

    /**
     * Put element in cache with specified TTL. If TTL is not greater than zero,
     * the default TTL will be used.
     */
    void put(CacheEntry<K, V> entry, long ttl, TimeUnit timeUnit) throws CacheStoreUnavailableException;
	boolean remove(K key) throws CacheStoreUnavailableException;
	
	CacheStatistics getStatistics() throws CacheStoreUnavailableException;
	
	void setMaxSize(int max);
	boolean clear() throws CacheStoreUnavailableException;
	boolean contains(K key) throws CacheStoreUnavailableException;

    String getName();
	
	void shutdown();
}
