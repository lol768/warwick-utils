package uk.ac.warwick.util.cache.memcached;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.warwick.util.cache.BasicCache;
import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntry;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.core.jodatime.DateTimeUtils;

import java.time.Instant;

import static org.junit.Assert.*;

@Ignore("Doesn't work on Bamboo")
public class MemcachedCacheStoreServerDownTest {

    private static MemcachedCacheStore<String, String> cacheStore;

    @BeforeClass
    public static void setup() throws Exception {
        MemcachedClient client = new MemcachedClient(
            new ConnectionFactoryBuilder()
                .setOpTimeout(100)
                .build(),
            AddrUtil.getAddresses("localhost:12345")
        );
        cacheStore = new MemcachedCacheStore("cacheName", Integer.MAX_VALUE, client);
    }

    private int cacheCallCount = 0;

    // Use an actual cache, rather than going to the store directly
    private Cache<String, String> cache;

    @Before
    public void setUpCache() throws Exception {
        cache  = new BasicCache<>(cacheStore, Caches.wrapFactoryWithoutDataInitialisation(new SingularCacheEntryFactory<String, String>() {
            public String create(String key) throws CacheEntryUpdateException {
                cacheCallCount++;
                return key.substring(6);
            }

            public boolean shouldBeCached(String val) {
                return true;
            }
        }), 10);
    }

    @Test
    public void get() throws Exception {
        assertEquals("12345", cache.get("token:12345"));
        assertEquals(1, cacheCallCount);

        assertEquals("12345", cache.get("token:12345"));
        assertEquals(2, cacheCallCount);
    }

    @Test
    public void getResult() throws Exception {
        DateTimeUtils.useMockDateTime(Instant.now(), () -> {
            try {
                Cache.Result<String> result = cache.getResult("token:12345");
                assertEquals(Instant.now(DateTimeUtils.CLOCK_IMPLEMENTATION).toEpochMilli(), result.getLastUpdated());
                assertEquals("12345", result.getValue());
                assertFalse(result.isUpdating());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void put() throws Exception {
        cache.put(new CacheEntry<String, String>("token:12345", "12345"));
    }

    @Test
    public void remove() throws Exception {
        cache.remove("token:12345");
    }

    @Test
    public void clear() throws Exception {
        cache.clear();
    }

    @Test
    public void contains() throws Exception {
        assertFalse(cache.contains("token:12345"));
        cache.put(new CacheEntry<String, String>("token:12345", "12345"));
        assertFalse(cache.contains("token:12345"));
    }
}
