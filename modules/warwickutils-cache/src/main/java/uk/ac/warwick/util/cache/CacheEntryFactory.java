package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface to a factory that will create a value appropriate
 * to be placed in the given key of the cache. It should
 * create a new object each time.
 * <p>
 * T is a type of exception that this factory may throw.
 * If you don't have any required exceptions that need to be
 * declared, you can put RuntimeException here.
 */
public interface CacheEntryFactory<K extends Serializable,V extends Serializable> {
	V create(K key) throws CacheEntryUpdateException;
	
	/**
	 * If supported. If not supported, this should throw
	 * {@link UnsupportedOperationException} and {@link #isSupportsMultiLookups()}
	 * should return false.
	 */
	Map<K,V> create(List<K> keys) throws CacheEntryUpdateException;
	
	/**
	 * @return Whether this factory supports the {@link #create(List)}
	 * 	method. If false, that method should throw an {@link UnsupportedOperationException}. 
	 */
	boolean isSupportsMultiLookups();
	
	/**
	 * Allows the EntryFactory to tell the cache whether this value
	 * should be stored in the cache, or it should just be passed back
	 * without caching.
	 * 
	 * The vast majority of the time this should return true, but
	 * there are cases such as when a lookup only partially succeeds,
	 * that we want to return an object but we don't want to cache it,
	 * such as an UnverifiedUser.
	 */
	boolean shouldBeCached(V val);
}
