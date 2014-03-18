package uk.ac.warwick.util.cache;

import java.io.Serializable;

/**
 * {@link CacheStore} implements a simple store of cache elements - 
 * it doesn't do anything clever with them.  
 * 
 * Implementations must be thread safe, in that concurrent calls mustn't
 * result in corrupt data or infinite loops. 
 */
public interface CacheStore<K extends Serializable,V extends Serializable> {
	CacheEntry<K,V> get(K key) throws CacheStoreUnavailableException;
	void put(CacheEntry<K,V> entry) throws CacheStoreUnavailableException;
	boolean remove(K key) throws CacheStoreUnavailableException;
	
	CacheStatistics getStatistics() throws CacheStoreUnavailableException;
	
	void setMaxSize(int max);
	boolean clear() throws CacheStoreUnavailableException;
	boolean contains(K key) throws CacheStoreUnavailableException;

    String getName();
	
	void shutdown();
}
