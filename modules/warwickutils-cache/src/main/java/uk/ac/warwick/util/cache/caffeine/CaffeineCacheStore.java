package uk.ac.warwick.util.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.cache.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache implementation that uses an in-memory frequency/recency biased cache.
 */
public class CaffeineCacheStore<K extends Serializable, V extends Serializable> implements CacheStore<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineCacheStore.class);

    private static final ConcurrentMap<String, com.github.benmanes.caffeine.cache.Cache<?, ?>> caches =
        new ConcurrentHashMap<>();

    public static class Builder<K extends Serializable, V extends Serializable, T> implements Caches.Builder<K, V, T> {
        private final String name;
        private CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory;
        private CacheExpiryStrategy<K, V> expiryStrategy = TTLCacheExpiryStrategy.eternal();
        private long maximumSize = 0;
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
            LOGGER.warn("Properties can only be set with Memcached cache stores - ignoring");
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public CaffeineCacheStore<K, V> buildStore() {
            return new CaffeineCacheStore<>(name, (com.github.benmanes.caffeine.cache.Cache<K, CacheEntry<K, V>>) caches.computeIfAbsent(name, n -> {
                Caffeine<Object, Object> builder = Caffeine.newBuilder();
                if (maximumSize > 0) {
                    builder.maximumSize(maximumSize);
                }
                return builder.build();
            }));
        }

        @Override
        public CacheWithDataInitialisation<K, V, T> build() {
            return new BasicCache<>(buildStore(), entryFactory, expiryStrategy, asynchronousUpdateEnabled, asynchronousOnly);
        }
    }

    private final String cacheName;
    private final com.github.benmanes.caffeine.cache.Cache<K, CacheEntry<K, V>> caffeineCache;

    CaffeineCacheStore(String name, com.github.benmanes.caffeine.cache.Cache<K, CacheEntry<K, V>> caffeineCache) {
        this.cacheName = name;
        this.caffeineCache = caffeineCache;
    }

    @Override
    public CacheEntry<K, V> get(K key) {
        return caffeineCache.getIfPresent(key);
    }

    @Override
    public void put(CacheEntry<K, V> entry, Duration ttl) {
        // ttl is ignored here
        caffeineCache.put(entry.getKey(), entry);
    }

    @Override
    public boolean remove(K key) {
        caffeineCache.invalidate(key);
        return true;
    }

    @Override
    public CacheStatistics getStatistics() {
        return new CacheStatistics(caffeineCache.estimatedSize());
    }

    public void setMaxSize(int max) {
        LOGGER.warn("setMaxSize() called on CaffeineCacheStore which does not support it");
    }

    @Override
    public boolean clear() {
        caffeineCache.invalidateAll();
        return true;
    }

    @Override
    public boolean contains(K key) {
        return caffeineCache.asMap().containsKey(key);
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public void shutdown() {
        clear();
    }

    @VisibleForTesting
    public com.github.benmanes.caffeine.cache.Cache<K, CacheEntry<K, V>> getCaffeineCache() {
        return caffeineCache;
    }
}
