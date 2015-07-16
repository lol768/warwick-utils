package uk.ac.warwick.util.cache.memcached;

import net.spy.memcached.*;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.cache.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedCacheStore.class);

    private static final int SIZE_INFO_THRESHOLD = 100 * 1024; // 100kb

    private static final int SIZE_WARN_THRESHOLD = 2 * 1024 * 1024; // 2mb

    private static final String MD5_ALGORITHM_NAME = "MD5";

    private static final char[] HEX_CHARS =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

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

        String keyAsString = md5DigestAsHex(encodedKey);
        return getName() + ":" + keyAsString;
    }

    public CacheEntry<K, V> get(K key) {
        try {
            Object value = memcachedClient.get(prefix(key));
            if (value == null || value instanceof String) { // Bug in jmemcached - when using binary protocol, returns empty strings
                return null;
            }
            return (CacheEntry<K, V>) value;
        } catch (CancellationException e) {
            // Do nothing, treat as cache miss
        } catch (OperationTimeoutException e) {
            // Do nothing, treat as cache miss
        } catch (RuntimeException e) {
            // Gee, thanks spymemcached for wrapping the nice OperationTimeoutException in a RuntimeException
            // Do nothing, treat as cache miss
        }

        return null;
    }

    public void put(final CacheEntry<K, V> entry, long ttl, TimeUnit timeUnit) {
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

        // FIXME Wasteful double-encode
        Transcoder<CacheEntry<K, V>> oneTimeTranscoder = new Transcoder<CacheEntry<K, V>>() {
            private CachedData data;

            @Override
            public CachedData encode(CacheEntry<K, V> o) {
                if (o != entry) {
                    throw new IllegalStateException();
                }

                if (data != null) {
                    return data;
                } else {
                    data = memcachedClient.getTranscoder().encode(o);

                    return data;
                }
            }

            @Override
            public int getMaxSize() {
                return memcachedClient.getTranscoder().getMaxSize();
            }

            @Override
            public CacheEntry<K, V> decode(CachedData d) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean asyncDecode(CachedData d) {
                throw new UnsupportedOperationException();
            }
        };

        // Size check
        int size = oneTimeTranscoder.encode(entry).getData().length;
        if (size > SIZE_WARN_THRESHOLD) {
            // Get it to print a stack trace, which is cheating a bit
            LOGGER.warn("Very large cache item stored in memcached (" + size + " bytes)", new Throwable());
        } else if (size > SIZE_INFO_THRESHOLD) {
            LOGGER.info("Large cache item stored in memcached (" + size + " bytes)", new Throwable());
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cache item stored in memcached (" + size + " bytes)", new Throwable());
        }

        memcachedClient.set(prefix(entry.getKey()), ttlSeconds, entry, oneTimeTranscoder);
    }

    public boolean remove(K key) {
        try {
            return memcachedClient.delete(prefix(key)).get();
        } catch (InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            return false;
        } catch (RuntimeException e) {
            // Gee, thanks spymemcached for wrapping the nice OperationTimeoutException in a RuntimeException
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
        } catch (RuntimeException e) {
            // Gee, thanks spymemcached for wrapping the nice OperationTimeoutException in a RuntimeException
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

    public static final MemcachedClient getDefaultMemcachedClient() {
        return defaultMemcachedClient;
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

    // Some methods from Spring DigestUtils to avoid a dependency on Spring
    private static String md5DigestAsHex(byte[] bytes) {
        return digestAsHexString(MD5_ALGORITHM_NAME, bytes);
    }

    private static String digestAsHexString(String algorithm, byte[] bytes) {
        char[] hexDigest = digestAsHexChars(algorithm, bytes);
        return new String(hexDigest);
    }

    /**
     * Creates a new {@link MessageDigest} with the given algorithm. Necessary
     * because {@code MessageDigest} is not thread-safe.
     */
    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
        }
    }

    private static byte[] digest(String algorithm, byte[] bytes) {
        return getDigest(algorithm).digest(bytes);
    }

    private static char[] digestAsHexChars(String algorithm, byte[] bytes) {
        byte[] digest = digest(algorithm, bytes);
        return encodeHex(digest);
    }

    private static char[] encodeHex(byte[] bytes) {
        char chars[] = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }
}
