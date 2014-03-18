package uk.ac.warwick.util.cache.memcached;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import uk.ac.warwick.util.cache.MemcachedUtils;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Random;

import static org.junit.Assert.*;

public class AbstractMemcachedCacheStoreTest<K extends Serializable, V extends Serializable> {

    private static final String CACHE_NAME = "customCache";

    protected static final MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();

    private static InetSocketAddress memcachedAddress;

    protected MemcachedCacheStore<K, V> cacheStore;

    protected MemcachedClient client;

    @BeforeClass
    public static void setupMemcachedServerAndClient() {
        CacheStorage<Key, LocalCacheElement> storage =
                ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 1000, 16384);
        daemon.setCache(new CacheImpl(storage));
        daemon.setBinary(false);

        Random r = new Random();
        int low = 20000;
        int high = 30000;
        int port = r.nextInt(high - low) + low;

        memcachedAddress = new InetSocketAddress("localhost", port);

        daemon.setAddr(memcachedAddress);
        daemon.setIdleTime(30000);
        daemon.setVerbose(true);
        daemon.start();
    }

    @AfterClass
    public static void shutdownMemcachedServerAndClient() {
        daemon.stop();
    }

    @Before
    public void setUp() throws Exception {
        MemcachedUtils.setUp();
        client = new MemcachedClient(memcachedAddress);
        cacheStore = new MemcachedCacheStore<K, V>(CACHE_NAME, 10, client);
    }

    @After
    public void tearDown() {
        cacheStore.clear();
        cacheStore.shutdown();
        MemcachedUtils.tearDown();

        // To reset the stats
        daemon.stop();
        daemon.start();
    }

    protected void assertSize(int size) {
        assertEquals(size, cacheStore.getStatistics().getCacheSize());
    }

}
