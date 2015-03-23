package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.util.cache.ehcache.EhCacheStore;
import uk.ac.warwick.util.cache.memcached.MemcachedCacheStore;

/**
 * Cache factory methods.
 */
public final class Caches {
    
    public enum CacheStrategy {
        EhCacheIfAvailable,
        EhCacheRequired,
        MemcachedIfAvailable,
        MemcachedRequired,
        InMemoryOnly
    };
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Caches.class);
	
	private static boolean ehChecked;
	private static boolean ehAvailable;

    private static boolean memcachedChecked;
    private static boolean memcachedAvailable;
	
	private Caches() {}
	
	public static <K extends Serializable,V extends Serializable> Cache<K, V> newCache(String name, CacheEntryFactory<K,V> factory, long timeout) {
	    return newCache(name, wrapFactoryWithoutDataInitialisation(factory), timeout, CacheStrategy.EhCacheIfAvailable);
	}
	
	public static <K extends Serializable,V extends Serializable> Cache<K, V> newCache(String name, CacheEntryFactory<K,V> factory, long timeout, CacheStrategy cacheStrategy) {
		return new BasicCache<K, V, Object>(name, wrapFactoryWithoutDataInitialisation(factory), timeout, cacheStrategy);
	}

    public static <K extends Serializable,V extends Serializable,T> CacheWithDataInitialisation<K, V, T> newDataInitialisatingCache(String name, CacheEntryFactoryWithDataInitialisation<K,V,T> factory, long timeout) {
        return newDataInitialisatingCache(name, factory, timeout, CacheStrategy.EhCacheIfAvailable);
    }

    public static <K extends Serializable,V extends Serializable,T> CacheWithDataInitialisation<K, V, T> newDataInitialisatingCache(String name, CacheEntryFactoryWithDataInitialisation<K,V,T> factory, long timeout, CacheStrategy cacheStrategy) {
        return new BasicCache<K, V, T>(name, factory, timeout, cacheStrategy);
    }

    public static <K extends Serializable,V extends Serializable> CacheEntryFactoryWithDataInitialisation<K, V, Object> wrapFactoryWithoutDataInitialisation(final CacheEntryFactory<K,V> factory) {
        if (factory == null) { return null; }

        return new CacheEntryFactoryWithDataInitialisation<K, V, Object>() {
            @Override
            public V create(K key, Object data) throws CacheEntryUpdateException {
                return factory.create(key);
            }

            @Override
            public Map<K, V> create(List<K> keys) throws CacheEntryUpdateException {
                return factory.create(keys);
            }

            @Override
            public boolean isSupportsMultiLookups() {
                return factory.isSupportsMultiLookups();
            }

            @Override
            public boolean shouldBeCached(V val) {
                return factory.shouldBeCached(val);
            }
        };
    }
	
	/**
	 * Creates a new cache. If Ehcache is available, it is used - reflection is used
	 * to avoid attempting to classload any EhCache classes until we know it's actually
	 * available.
	 */
	public static <K extends Serializable,V extends Serializable> CacheStore<K, V> newCacheStore(String name, long timeoutSeconds, CacheStrategy cacheStrategy) {
		switch (cacheStrategy) {
		    case EhCacheRequired:
		        if (isEhCacheAvailable()) {
		            return new EhCacheStore<K, V>(name);
		        }
		        
		        throw new IllegalStateException("EhCache unavailable for " + name);
		    case EhCacheIfAvailable:
		        if (isEhCacheAvailable()) {
		            LOGGER.info("Ehcache detected - using EhCacheStore for " + name + ".");
                    return new EhCacheStore<K, V>(name);
                }

	            LOGGER.info("Ehcache not found - using built in cache store for " + name + ".");
                return new HashMapCacheStore<K, V>(name);
            case MemcachedRequired:
                if (isMemcachedAvailable()) {
                    return new MemcachedCacheStore<K, V>(name, (int) timeoutSeconds);
                }

                throw new IllegalStateException("memcached unavailable for " + name);
            case MemcachedIfAvailable:
                if (isMemcachedAvailable()) {
                    LOGGER.info("memcached detected - using MemcachedCacheStore for " + name + ".");
                    return new MemcachedCacheStore<K, V>(name, (int) timeoutSeconds);
                }

                LOGGER.info("memcached not found - using built in cache store for " + name + ".");
                return new HashMapCacheStore<K, V>(name);
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

    /**
     * Attempt to dynamically classload one of the main spymemcached classes, to
     * see if it's available to the default classloader.
     */
    public static boolean isMemcachedAvailable() {
        if (!memcachedChecked) {
            try {
                Class.forName("net.spy.memcached.MemcachedClient");
                memcachedAvailable = true;
            } catch (ClassNotFoundException e) {
                memcachedAvailable = false;
            }
            memcachedChecked = true;
        }
        return memcachedAvailable;
    }

    public static void resetMemcachedCheck() {
        memcachedChecked = false;
    }
}
