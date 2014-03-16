package uk.ac.warwick.util.cache.memcached;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
import net.spy.memcached.MemcachedClient;
import org.junit.*;
import uk.ac.warwick.util.cache.CacheEntry;
import uk.ac.warwick.util.cache.MemcachedUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class MemcachedCacheStoreTest {

    private static final String CACHE_NAME = "customCache";

    private static final MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();

    private static InetSocketAddress memcachedAddress;

    private MemcachedCacheStore<String, String> cache;

    private MemcachedClient client;

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
        cache = new MemcachedCacheStore<String, String>(CACHE_NAME, 10, client);
    }

    @After
    public void tearDown() {
        cache.clear();
        cache.shutdown();
        MemcachedUtils.tearDown();
    }

    @Test
    public void creation() throws Exception {
        CacheEntry<String, String> string = cache.get("token:abcdefghij");
        assertNull(string);

        CacheEntry<String, String> entry = new CacheEntry<String, String>("token:12345", "Johnny");
        cache.put(entry);

        assertEquals(entry.getValue(), cache.get("token:12345").getValue());
        assertEquals(entry.getValue(), cache.get("token:12345").getValue());

        // Check the cache stats are right
        assertSize(1);

        Map<String, String> stats = client.getStats().values().iterator().next();
        assertEquals("2", stats.get("get_hits"));
        assertEquals("1", stats.get("get_misses"));

        cache.clear();
        assertSize(0);
    }

    private void assertSize(int size) {
        assertEquals(size, cache.getStatistics().getCacheSize());
    }
}
