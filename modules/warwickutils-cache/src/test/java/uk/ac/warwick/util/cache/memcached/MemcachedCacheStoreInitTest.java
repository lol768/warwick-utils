package uk.ac.warwick.util.cache.memcached;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
import org.junit.*;
import uk.ac.warwick.util.cache.CacheEntry;
import uk.ac.warwick.util.cache.MemcachedUtils;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class MemcachedCacheStoreInitTest {

    private static final int PORT = 44444; // FIXME this is hardcoded because it needs to be in the properties file, needs to match

    private static final MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();

    @BeforeClass
    public static void setupMemcachedServer() {
        CacheStorage<Key, LocalCacheElement> storage =
                ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 1000, 16384);
        daemon.setCache(new CacheImpl(storage));
        daemon.setAddr(new InetSocketAddress("localhost", PORT));
        daemon.setIdleTime(30000);
        daemon.setVerbose(true);
        daemon.setBinary(true);
        daemon.start();
    }

    @AfterClass
    public static void shutdownMemcachedServer() {
        daemon.stop();
    }

    @Before
    public void setUp() throws Exception {
        MemcachedUtils.setUp();
    }

    @After
    public void tearDown() {
        MemcachedUtils.tearDown();
    }

    @Test
    public void init() throws Exception {
        MemcachedCacheStore<String, String> cache = new MemcachedCacheStore<String, String>("testCache", 10);

        CacheEntry<String, String> string = cache.get("token:abcdefghij");
        assertNull(string);

        CacheEntry<String, String> entry = new CacheEntry<String, String>("token:12345", "Johnny");
        cache.put(entry);

        assertEquals(entry.getValue(), cache.get("token:12345").getValue());
        assertEquals(entry.getValue(), cache.get("token:12345").getValue());

        // Check the cacheStore stats are right
        assertEquals(1, cache.getStatistics().getCacheSize());

        cache.clear();
        assertEquals(0, cache.getStatistics().getCacheSize());
    }


}
