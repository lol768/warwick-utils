package uk.ac.warwick.util.cache;

import java.io.Serializable;

import org.apache.log4j.Logger;

import uk.ac.warwick.util.cache.ehcache.EhCacheStore;

/**
 * Cache factory methods.
 */
public final class Caches {
    
    public enum CacheStrategy {
        EhCacheIfAvailable,
        EhCacheRequired,
        InMemoryOnly
    };
	
	private static final Logger LOGGER = Logger.getLogger(Caches.class);
	
	private static boolean ehChecked;
	private static boolean ehAvailable;
	
	private Caches() {}
	
	public static <K extends Serializable,V extends Serializable> BasicCache<K, V> newCache(String name, CacheEntryFactory<K,V> factory, long timeout) {
	    return newCache(name, factory, timeout, CacheStrategy.EhCacheIfAvailable);
	}
	
	public static <K extends Serializable,V extends Serializable> BasicCache<K, V> newCache(String name, CacheEntryFactory<K,V> factory, long timeout, CacheStrategy cacheStrategy) {
		return new BasicCache<K, V>(name, factory, timeout, cacheStrategy);
	}
	
	/**
	 * Creates a new cache. If Ehcache is available, it is used - reflection is used
	 * to avoid attempting to classload any EhCache classes until we know it's actually
	 * available.
	 */
	public static <K extends Serializable,V extends Serializable> CacheStore<K, V> newCacheStore(String name, CacheStrategy cacheStrategy) {
		switch (cacheStrategy) {
		    case EhCacheRequired:
		        if (isEhCacheAvailable()) {
		            return new EhCacheStore<K, V>(name);
		        }
		        
		        throw new IllegalStateException("EhCache unavailable");
		    case EhCacheIfAvailable:
		        if (isEhCacheAvailable()) {
		            LOGGER.info("Ehcache detected - using EhCacheStore.");
                    return new EhCacheStore<K, V>(name);
                }

                // Intentional fall-through here
	            LOGGER.info("Ehcache not found - using built in cache store.");
		    case InMemoryOnly:
		        return new HashMapCacheStore<K, V>(name);
		        
		    default:
		        throw new IllegalArgumentException("Unexpected cache strategy: " + cacheStrategy);
		}
	}

	/**
	 * Attempt to dynamically classload one of the main Ehcache classes, to
	 * see if it's available to the default classloader.
	 */
	public static boolean isEhCacheAvailable() {
		if (!ehChecked) {			
			try {
				Class.forName("net.sf.ehcache.CacheManager");
				String storeDir = System.getProperty("ehcache.disk.store.dir");
				String warwickDir = System.getProperty("warwick.ehcache.disk.store.dir");
				if (storeDir == null && warwickDir == null) {
					LOGGER.info("Ehcache found but no disk cache location specified with ehcache.disk.store.dir or warwick.ehcache.disk.store.dir, so not using Ehcache");
					ehAvailable = false;
				} else {
					ehAvailable = true;
				}
			} catch (ClassNotFoundException e) {
				ehAvailable = false;
			}
			ehChecked = true;
		}
		return ehAvailable;
	}
	
	public static void resetEhCacheCheck() {
	    ehChecked = false;
	}
}
