package uk.ac.warwick.util.cache.memcached;

import net.spy.memcached.*;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.apache.log4j.Logger;
import org.springframework.util.DigestUtils;
import uk.ac.warwick.util.cache.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache implementation that uses the spymemcached library to connect to memcached.
 */
public final class MemcachedCacheStore<K extends Serializable, V extends Serializable> implements CacheStore<K, V> {

    /**
     * The classpath location that this class will check in for a custom memcached
     * configuration file.
     */
    public static final String CUSTOM_CONFIG_URL = "memcached.properties";

    private static final Logger LOGGER = Logger.getLogger(MemcachedCacheStore.class);

    private static MemcachedClient defaultMemcachedClient;

    private final String cacheName;

    private final int timeoutInSeconds;

    private final MemcachedClient memcachedClient;

    MemcachedCacheStore(final String name, final int timeout, final MemcachedClient client) {
        this.cacheName = name;
        this.timeoutInSeconds = timeout;
        this.memcachedClient = client;
        init();
    }

    /**
     * Creates a MemcachedCacheStore using a shared MemcachedClient loaded from either
     * a default classpath location, or one specified by the system property
     * warwick.memcached.config. Subsequent MemcachedCacheStores created with this
     * constructor will use the same MemcachedClient.
     */
    public MemcachedCacheStore(final String name, final int timeout) {
        this.cacheName = name;
        this.timeoutInSeconds = timeout;
        if (defaultMemcachedClient == null) {
            // Load the default properties first, then override
            Properties properties = new Properties();
            try {
                properties.load(getClass().getResourceAsStream("/memcached-default.properties"));
            } catch (IOException e) {
                throw new IllegalStateException("Couldn't load default properties for memcached", e);
            }

            String location = System.getProperty("warwick.memcached.config");

            InputStream customPropertiesStream;
            if (location == null || location.equals("")) {
                customPropertiesStream = getClass().getResourceAsStream(CUSTOM_CONFIG_URL);
            } else {
                customPropertiesStream = getClass().getResourceAsStream(location);
            }

            if (customPropertiesStream != null) {
                Properties customProperties = new Properties();

                try {
                    customProperties.load(customPropertiesStream);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not load configuration from " + ((location == null || location.equals("")) ? CUSTOM_CONFIG_URL : location));
                }

                for (Enumeration en = customProperties.propertyNames(); en.hasMoreElements();) {
                    String key = (String) en.nextElement();
                    String value = customProperties.getProperty(key);

                    properties.setProperty(key, value);
                }
            }

            SerializingTranscoder transcoder = new SerializingTranscoder();
            transcoder.setCompressionThreshold(Integer.valueOf(properties.getProperty("memcached.transcoder.compressionThreshold")));

            ConnectionFactory connectionFactory =
                new ConnectionFactoryBuilder()
                    .setDaemon(Boolean.valueOf(properties.getProperty("memcached.daemon")))
                    .setFailureMode(FailureMode.valueOf(properties.getProperty("memcached.failureMode")))
                    .setHashAlg(HashAlgorithmRegistry.lookupHashAlgorithm(properties.getProperty("memcached.hashAlgorithm")))
                    .setLocatorType(ConnectionFactoryBuilder.Locator.valueOf(properties.getProperty("memcached.locatorType")))
                    .setMaxReconnectDelay(Long.valueOf(properties.getProperty("memcached.maxReconnectDelay")))
                    .setOpQueueMaxBlockTime(Long.valueOf(properties.getProperty("memcached.opQueueMaxBlockTime")))
                    .setOpTimeout(Long.valueOf(properties.getProperty("memcached.opTimeout")))
                    .setProtocol(ConnectionFactoryBuilder.Protocol.valueOf(properties.getProperty("memcached.protocol")))
                    .setReadBufferSize(Integer.valueOf(properties.getProperty("memcached.readBufferSize")))
                    .setShouldOptimize(Boolean.valueOf(properties.getProperty("memcached.shouldOptimize")))
                    .setTimeoutExceptionThreshold(Integer.valueOf(properties.getProperty("memcached.timeoutExceptionThreshold")))
                    .setUseNagleAlgorithm(Boolean.valueOf(properties.getProperty("memcached.useNagleAlgorithm")))
                    .setTranscoder(transcoder)
                    .build();

            String servers = properties.getProperty("memcached.servers");

            try {
                defaultMemcachedClient =
                    new MemcachedClient(connectionFactory, AddrUtil.getAddresses(servers));
            } catch (IOException e) {
                throw new IllegalStateException("Couldn't connect to memcached", e);
            }
        }
        this.memcachedClient = defaultMemcachedClient;
        init();
    }

    public void init() {
        // Nothing to do
    }

    private String prefix(K key) {
        byte[] encodedKey = memcachedClient.getTranscoder().encode(key).getData();

        // FIXME this requires spring.util but that seems excessive, particularly if we weren't using Spring
        String keyAsString = DigestUtils.md5DigestAsHex(encodedKey);
        return getName() + ":" + keyAsString;
    }

    public CacheEntry<K, V> get(K key) throws CacheStoreUnavailableException {
        try {
            Object value = memcachedClient.get(prefix(key));
            if (value == null || value instanceof String) { // Bug in jmemcached - when using binary protocol, returns empty strings
                return null;
            }
            return (CacheEntry<K, V>) value;
        } catch (CancellationException e) {
            throw new CacheStoreUnavailableException(e);
        } catch (OperationTimeoutException e) {
            throw new CacheStoreUnavailableException(e);
        }
    }

    public void put(CacheEntry<K, V> entry, long ttl, TimeUnit timeUnit) {
        final int ttlSeconds;

        if (ttl > 0) {
            // If explicit TTL set, use that
            long ttlInSeconds = (int) timeUnit.toSeconds(ttl);

            if (ttlInSeconds > Integer.MAX_VALUE) {
                ttlSeconds = Integer.MAX_VALUE;
            } else {
                ttlSeconds = (int) ttlInSeconds;
            }
        } else if (ttl == CacheEntryFactory.TIME_TO_LIVE_ETERNITY) {
            ttlSeconds = Integer.MAX_VALUE;
        } else {
            ttlSeconds = timeoutInSeconds;
        }

        memcachedClient.set(prefix(entry.getKey()), ttlSeconds, entry);
    }

    public boolean remove(K key) {
        try {
            return memcachedClient.delete(prefix(key)).get();
        } catch (InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public CacheStatistics getStatistics() throws CacheStoreUnavailableException {
        Map<SocketAddress, Map<String, String>> allStats = memcachedClient.getStats();

        long totalSize = 0;
        int unavailableStores = 0;
        for (Map<String, String> stats: allStats.values()) {
            // There is a bug in jmemcached that returns this stat as "cur_items"
            try {
                if (stats.containsKey("curr_items")) {
                    totalSize += Long.parseLong(stats.get("curr_items"));
                } else {
                    totalSize += Long.parseLong(stats.get("cur_items"));
                }
            } catch (NumberFormatException e) {
                unavailableStores++;
            }
        }

        if (unavailableStores == allStats.size()) {
            throw new CacheStoreUnavailableException("No memcached backends available");
        }

        return new CacheStatistics(totalSize);
    }

    public void setMaxSize(int max) {
        LOGGER.warn("setMaxSize() called on MemcachedCacheStore which does not support it");
    }

    public boolean clear() {
        try {
            return memcachedClient.flush().get();
        } catch (InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public boolean contains(K key) throws CacheStoreUnavailableException {
        return get(key) != null;
    }

    public String getName() {
        return cacheName;
    }

    public void shutdown() {
        LOGGER.info("Shutting down MemcachedClient");
        if (memcachedClient != defaultMemcachedClient) {
            memcachedClient.shutdown();
        }
    }

    /**
     * Normally you can allow the shutdown hooks to
     */
    public static final void shutdownDefaultMemcachedClient() {
        if (defaultMemcachedClient != null) {
            defaultMemcachedClient.shutdown();
            defaultMemcachedClient = null;
        }
    }
}
