package uk.ac.warwick.util.cache.memcached;

import net.spy.memcached.*;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.apache.log4j.Logger;
import uk.ac.warwick.util.cache.CacheEntry;
import uk.ac.warwick.util.cache.CacheStatistics;
import uk.ac.warwick.util.cache.CacheStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Cache implementation that uses the spymemcached library to connect to memcached.
 */
public final class MemcachedCacheStore<K extends Serializable,V extends Serializable> implements CacheStore<K, V> {

    /**
     * The classpath location that this class will check in for a custom memcached
     * configuration file.
     */
    public static final String CUSTOM_CONFIG_URL = "memcached.properties";

    private static final Logger LOGGER = Logger.getLogger(MemcachedCacheStore.class);

    private static MemcachedClient defaultMemcachedClient;

    private final String cacheName;

    private final MemcachedClient memcachedClient;

    MemcachedCacheStore(final String name, final MemcachedClient client) {
        this.cacheName = name;
        this.memcachedClient = client;
        init();
    }

    /**
     * Creates a MemcachedCacheStore using a shared MemcachedClient loaded from either
     * a default classpath location, or one specified by the system property
     * warwick.memcached.config. Subsequent MemcachedCacheStores created with this
     * constructor will use the same MemcachedClient.
     */
    public MemcachedCacheStore(final String name) {
        this.cacheName = name;
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

    @Override
    public CacheEntry<K, V> get(K key) {
        return null;
    }

    @Override
    public void put(CacheEntry<K, V> entry) {

    }

    @Override
    public boolean remove(K key) {
        return false;
    }

    @Override
    public CacheStatistics getStatistics() {
        return null;
    }

    @Override
    public void setMaxSize(int max) {

    }

    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public boolean contains(K key) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
