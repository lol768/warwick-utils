package uk.ac.warwick.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.cache.caffeine.CaffeineCacheStore;
import uk.ac.warwick.util.cache.memcached.MemcachedCacheStore;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Cache factory methods.
 */
public final class Caches {
    
    public enum CacheStrategy {
        @Deprecated EhCacheIfAvailable,
        @Deprecated EhCacheRequired,
        CaffeineIfAvailable,
        CaffeineRequired,
        MemcachedIfAvailable,
        MemcachedRequired,
        InMemoryOnly
    }
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Caches.class);
	
	private static boolean caffeineChecked;
	private static boolean caffeineAvailable;

    private static boolean memcachedChecked;
    private static boolean memcachedAvailable;
	
	private Caches() {}

	public interface Builder<K extends Serializable, V extends Serializable, T> {
	    <U> Builder<K, V, U> dataInitialisingEntryFactory(CacheEntryFactoryWithDataInitialisation<K, V, U> entryFactory);
        default Builder<K, V, Object> entryFactory(CacheEntryFactory<K, V> entryFactory) {
            return dataInitialisingEntryFactory(wrapFactoryWithoutDataInitialisation(entryFactory));
        }
        Builder<K, V, T> expireAfterWrite(Duration duration);
        Builder<K, V, T> expiryStategy(CacheExpiryStrategy<K, V> expiryStrategy);
        Builder<K, V, T> maximumSize(long size);
        Builder<K, V, T> asynchronous();
        Builder<K, V, T> asynchronousOnly();
        Builder<K, V, T> properties(Properties properties);
        CacheStore<K, V> buildStore();
        CacheWithDataInitialisation<K, V, T> build();
    }

    public static <K extends Serializable, V extends Serializable> Builder<K, V, Object> builder(String name) {
        return builder(name, CacheStrategy.CaffeineIfAvailable);
    }

    public static <K extends Serializable, V extends Serializable> Builder<K, V, Object> builder(String name, CacheStrategy strategy) {
        return builderWithDataInitialisation(name, null, strategy);
    }

    public static <K extends Serializable, V extends Serializable> Builder<K, V, Object> builder(String name, CacheEntryFactory<K, V> entryFactory) {
        return builder(name, entryFactory, CacheStrategy.CaffeineIfAvailable);
    }

    public static <K extends Serializable, V extends Serializable> Builder<K, V, Object> builder(String name, CacheEntryFactory<K, V> entryFactory, CacheStrategy strategy) {
        return builderWithDataInitialisation(name, wrapFactoryWithoutDataInitialisation(entryFactory), strategy);
    }

    public static <K extends Serializable, V extends Serializable, T> Builder<K, V, T> builderWithDataInitialisation(String name, CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory) {
        return builderWithDataInitialisation(name, entryFactory, CacheStrategy.CaffeineIfAvailable);
    }

    public static <K extends Serializable, V extends Serializable, T> Builder<K, V, T> builderWithDataInitialisation(String name, CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory, CacheStrategy cacheStrategy) {
        switch (cacheStrategy) {
            case EhCacheRequired:
                throw new UnsupportedOperationException("CacheStrategy is EhCacheRequired but EhCache no longer supported.");
            case CaffeineRequired:
                if (isCaffeineAvailable()) {
                    return new CaffeineCacheStore.Builder<>(name, entryFactory);
                }

                throw new IllegalStateException("Caffeine unavailable for " + name);
            case EhCacheIfAvailable:
                LOGGER.warn("CacheStrategy EhCacheIfAvailable requested but no longer supported; using CaffeineIfAvailable");
                // Intentional drop-through
            case CaffeineIfAvailable:
                if (isCaffeineAvailable()) {
                    LOGGER.info("Caffeine detected - using CaffeineCacheStore for " + name + ".");
                    return new CaffeineCacheStore.Builder<>(name, entryFactory);
                }

                LOGGER.info("Caffeine not found - using built in cache store for " + name + ".");
                return new HashMapCacheStore.Builder<>(name, entryFactory);
            case MemcachedRequired:
                if (isMemcachedAvailable()) {
                    return new MemcachedCacheStore.Builder<>(name, entryFactory);
                }

                throw new IllegalStateException("memcached unavailable for " + name);
            case MemcachedIfAvailable:
                if (isMemcachedAvailable()) {
                    return new MemcachedCacheStore.Builder<>(name, entryFactory);
                }

                LOGGER.info("memcached not found - using built in cache store for " + name + ".");
                return new HashMapCacheStore.Builder<>(name, entryFactory);
            case InMemoryOnly:
                return new HashMapCacheStore.Builder<>(name, entryFactory);

            default:
                throw new IllegalArgumentException("Unexpected cache strategy: " + cacheStrategy);
        }
    }

    public static <K extends Serializable,V extends Serializable> CacheEntryFactoryWithDataInitialisation<K, V, Object> wrapFactoryWithoutDataInitialisation(final CacheEntryFactory<K, V> factory) {
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

    // Legacy initialisers
    @Deprecated
    public static <K extends Serializable,V extends Serializable> Cache<K, V> newCache(String name, CacheEntryFactory<K, V> factory, long timeout) {
        return newCache(name, wrapFactoryWithoutDataInitialisation(factory), timeout, CacheStrategy.EhCacheIfAvailable);
    }

    @Deprecated
    public static <K extends Serializable,V extends Serializable> Cache<K, V> newCache(String name, CacheEntryFactory<K,V> factory, long timeout, CacheStrategy cacheStrategy) {
        return builder(name, factory, cacheStrategy).expireAfterWrite(Duration.ofSeconds(timeout)).build();
    }

    @Deprecated
    public static <K extends Serializable,V extends Serializable> Cache<K, V> newCache(String name, CacheEntryFactory<K,V> factory, long timeout, CacheStrategy cacheStrategy, Properties properties) {
        return builder(name, factory, cacheStrategy).expireAfterWrite(Duration.ofSeconds(timeout)).properties(properties).build();
    }

    @Deprecated
    public static <K extends Serializable,V extends Serializable,T> CacheWithDataInitialisation<K, V, T> newDataInitialisatingCache(String name, CacheEntryFactoryWithDataInitialisation<K,V,T> factory, long timeout) {
        return newDataInitialisatingCache(name, factory, timeout, CacheStrategy.EhCacheIfAvailable);
    }

    @Deprecated
    public static <K extends Serializable,V extends Serializable,T> CacheWithDataInitialisation<K, V, T> newDataInitialisatingCache(String name, CacheEntryFactoryWithDataInitialisation<K,V,T> factory, long timeout, CacheStrategy cacheStrategy) {
        return builderWithDataInitialisation(name, factory, cacheStrategy).expireAfterWrite(Duration.ofSeconds(timeout)).build();
    }

    @Deprecated
    public static <K extends Serializable,V extends Serializable,T> CacheWithDataInitialisation<K, V, T> newDataInitialisatingCache(String name, CacheEntryFactoryWithDataInitialisation<K,V,T> factory, long timeout, CacheStrategy cacheStrategy, Properties properties) {
        return builderWithDataInitialisation(name, factory, cacheStrategy).expireAfterWrite(Duration.ofSeconds(timeout)).properties(properties).build();
    }

	/**
	 * Attempt to dynamically classload one of the main Caffeine classes, to
	 * see if it's available to the default classloader.
	 */
	public static boolean isCaffeineAvailable() {
		if (!caffeineChecked) {
			try {
				Class.forName("com.github.benmanes.caffeine.cache.Caffeine");
				caffeineAvailable = true;
			} catch (ClassNotFoundException e) {
                caffeineAvailable = false;
			}
			caffeineChecked = true;
		}
		return caffeineAvailable;
	}
	
	public static void resetCaffeineCheck() {
	    caffeineChecked = false;
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
