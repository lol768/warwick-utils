package uk.ac.warwick.util.cache.memcached;

import net.spy.memcached.*;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationQueueFactory;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Cache implementation that uses the spymemcached library to connect to memcached.
 */
public final class MemcachedCacheStore<K extends Serializable, V extends Serializable> implements CacheStore<K, V> {

    /**
     * The classpath location that this class will check in for a custom memcached
     * configuration file.
     */
    public static final String CUSTOM_CONFIG_URL = "/memcached.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedCacheStore.class);

    private static final int SIZE_INFO_THRESHOLD = 100 * 1024; // 100kb

    private static final int SIZE_WARN_THRESHOLD = 2 * 1024 * 1024; // 2mb

    private static final long MEMCACHED_TTL_SECONDS_THRESHOLD = TimeUnit.DAYS.toSeconds(30);

    private static final String MD5_ALGORITHM_NAME = "MD5";

    private static final char[] HEX_CHARS =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static MemcachedClient defaultMemcachedClient;

    public static class Builder<K extends Serializable, V extends Serializable, T> implements Caches.Builder<K, V, T> {
        private final String name;
        private CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory;
        private Duration expireAfterWrite = Duration.ofDays(30);
        private CacheExpiryStrategy<K, V> expiryStrategy;
        private Properties properties;
        private boolean asynchronousUpdateEnabled;
        private boolean asynchronousOnly;

        public Builder(String name, CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory) {
            this.name = name;
            this.entryFactory = entryFactory;
        }

        private Builder(String name, CacheEntryFactoryWithDataInitialisation<K, V, T> entryFactory, Duration expireAfterWrite, CacheExpiryStrategy<K, V> expiryStrategy, Properties properties, boolean asynchronousUpdateEnabled, boolean asynchronousOnly) {
            this(name, entryFactory);
            this.expireAfterWrite = expireAfterWrite;
            this.expiryStrategy = expiryStrategy;
            this.properties = properties;
            this.asynchronousUpdateEnabled = asynchronousUpdateEnabled;
            this.asynchronousOnly = asynchronousOnly;
        }

        @Override
        public <U> Builder<K, V, U> dataInitialisingEntryFactory(CacheEntryFactoryWithDataInitialisation<K, V, U> entryFactory) {
            return new Builder<>(name, entryFactory, expireAfterWrite, expiryStrategy, properties, asynchronousUpdateEnabled, asynchronousOnly);
        }

        @Override
        public Builder<K, V, T> expireAfterWrite(Duration duration) {
            this.expireAfterWrite = duration;
            return this;
        }

        @Override
        public Caches.Builder<K, V, T> expiryStategy(CacheExpiryStrategy<K, V> expiryStrategy) {
            this.expiryStrategy = expiryStrategy;
            return this;
        }

        @Override
        public Builder<K, V, T> maximumSize(long size) {
            throw new UnsupportedOperationException("Memcached doesn't support size-bound caches");
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
            this.properties = properties;
            return this;
        }

        @Override
        public MemcachedCacheStore<K, V> buildStore() {
            if (properties == null) {
                properties = customProperties();
            }

            return new MemcachedCacheStore<>(name, expireAfterWrite, properties);
        }

        @Override
        public Cache<K, V> build() {
            if (expiryStrategy == null) {
                expiryStrategy = TTLCacheExpiryStrategy.forTTL(expireAfterWrite);
            }

            return new BasicCache<>(buildStore(), entryFactory, expiryStrategy, asynchronousUpdateEnabled, asynchronousOnly);
        }
    }

    private final String cacheName;

    private final long timeoutInSeconds;

    private final MemcachedClient memcachedClient;

    MemcachedCacheStore(final String name, final Duration timeout, final MemcachedClient client) {
        this.cacheName = name;
        this.timeoutInSeconds = timeout.getSeconds();
        this.memcachedClient = client;
    }

    private static Properties customProperties() {
        String location = System.getProperty("warwick.memcached.config");

        InputStream customPropertiesStream;
        if (location == null || location.equals("")) {
            customPropertiesStream = MemcachedCacheStore.class.getResourceAsStream(CUSTOM_CONFIG_URL);
        } else {
            customPropertiesStream = MemcachedCacheStore.class.getResourceAsStream(location);
        }

        if (customPropertiesStream != null) {
            Properties customProperties = new Properties();

            try {
                customProperties.load(customPropertiesStream);
            } catch (IOException e) {
                throw new IllegalStateException("Could not load configuration from " + ((location == null || location.equals("")) ? CUSTOM_CONFIG_URL : location));
            }

            return customProperties;
        }

        return new Properties();
    }

    static class ConfigurableOperationQueueFactory implements OperationQueueFactory {
        private int capacity;

        ConfigurableOperationQueueFactory(int cap) {
            this.capacity = cap;
        }

        @Override
        public BlockingQueue<Operation> create() {
            if (capacity > 0) {
                return new ArrayBlockingQueue<>(capacity);
            } else {
                return new LinkedBlockingQueue<>();
            }
        }
    }

    /**
     * Creates a MemcachedCacheStore using a shared MemcachedClient loaded from either
     * a default classpath location, or one specified by the system property
     * warwick.memcached.config. Subsequent MemcachedCacheStores created with this
     * constructor will use the same MemcachedClient.
     */
    MemcachedCacheStore(final String name, final Duration timeout) {
        this(name, timeout, customProperties());
    }

    /**
     * Creates a MemcachedCacheStore using a shared MemcachedClient loaded from the
     * passed properties. Subsequent MemcachedCacheStores created with this
     * constructor will use the same MemcachedClient.
     */
    private MemcachedCacheStore(final String name, final Duration timeout, final Properties customProperties) {
        this.cacheName = name;
        this.timeoutInSeconds = timeout.getSeconds();
        if (defaultMemcachedClient == null) {
            // Load the default properties first, then override
            Properties properties = new Properties();
            try {
                properties.load(getClass().getResourceAsStream("/memcached-default.properties"));
            } catch (IOException e) {
                throw new IllegalStateException("Couldn't load default properties for memcached", e);
            }

            for (Enumeration en = customProperties.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                String value = customProperties.getProperty(key);

                properties.setProperty(key, value);
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
                    .setOpQueueFactory(new ConfigurableOperationQueueFactory(Integer.parseInt(properties.getProperty("memcached.maxOperationsQueueSize"))))
                    .setReadOpQueueFactory(new ConfigurableOperationQueueFactory(Integer.parseInt(properties.getProperty("memcached.maxOperationsQueueSize"))))
                    .setWriteOpQueueFactory(new ConfigurableOperationQueueFactory(Integer.parseInt(properties.getProperty("memcached.maxOperationsQueueSize"))))
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
    }

    private String prefix(K key) {
        byte[] encodedKey = memcachedClient.getTranscoder().encode(key).getData();

        String keyAsString = md5DigestAsHex(encodedKey);
        return getName() + ":" + keyAsString;
    }

    @SuppressWarnings("unchecked")
    public CacheEntry<K, V> get(K key) {
        try {
            Object value = memcachedClient.get(prefix(key));
            if (value == null || value instanceof String) { // Bug in jmemcached - when using binary protocol, returns empty strings
                return null;
            }
            return (CacheEntry<K, V>) value;
        } catch (OperationTimeoutException | CancellationException e) {
            // Do nothing, treat as cache miss
        } catch (RuntimeException e) {
            // Gee, thanks spymemcached for wrapping the nice OperationTimeoutException in a RuntimeException
            // Do nothing, treat as cache miss
        }

        return null;
    }

    public void put(final CacheEntry<K, V> entry, Duration ttl) {
        final int ttlSeconds;

        if (ttl.getSeconds() > 0) {
            // If explicit TTL set, use that
            long ttlInSeconds = ttl.getSeconds();

            if (ttlInSeconds > Integer.MAX_VALUE) {
                ttlSeconds = Integer.MAX_VALUE;
            } else if (ttlInSeconds > MEMCACHED_TTL_SECONDS_THRESHOLD) {
                // Unix timestamp
                ttlSeconds = (int) (Instant.now().getEpochSecond() + ttlInSeconds);
            } else {
                ttlSeconds = (int) ttlInSeconds;
            }
        } else if (ttl.equals(CacheEntryFactory.TIME_TO_LIVE_ETERNITY)) {
            ttlSeconds = Integer.MAX_VALUE;
        } else if (timeoutInSeconds > Integer.MAX_VALUE) {
            ttlSeconds = Integer.MAX_VALUE;
        } else {
            ttlSeconds = (int) timeoutInSeconds;
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
        } catch (RuntimeException | ExecutionException | InterruptedException e) {
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

    public boolean clear() {
        try {
            return memcachedClient.flush().get();
        } catch (RuntimeException | ExecutionException | InterruptedException e) {
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

    public static MemcachedClient getDefaultMemcachedClient() {
        return defaultMemcachedClient;
    }

    /**
     * Normally you can allow the shutdown hooks to
     */
    public static void shutdownDefaultMemcachedClient() {
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
        char[] chars = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }
}
