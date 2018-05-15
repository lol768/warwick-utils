package uk.ac.warwick.util.cache.memcached;

import org.junit.*;
import uk.ac.warwick.util.cache.CacheEntry;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MemcachedCacheStoreTest extends AbstractMemcachedCacheStoreTest<String, String> {

    @Test
    public void creation() throws Exception {
        CacheEntry<String, String> string = cacheStore.get("token:abcdefghij");
        assertNull(string);

        CacheEntry<String, String> entry = new CacheEntry<String, String>("token:12345", "Johnny");
        cacheStore.put(entry, 10, TimeUnit.SECONDS);

        assertEquals(entry.getValue(), cacheStore.get("token:12345").getValue());
        assertEquals(entry.getValue(), cacheStore.get("token:12345").getValue());

        // Check the cacheStore stats are right
        assertSize(1);

        Map<String, String> stats = client.getStats().values().iterator().next();
        assertEquals("2", stats.get("get_hits"));
        assertEquals("1", stats.get("get_misses"));

        cacheStore.clear();
        assertSize(0);
    }

    @Test
    public void longTTLs() throws Exception {
        // UTL-212
        CacheEntry<String, String> entry = new CacheEntry<>("token:999", "Paul");
        cacheStore.put(entry, 31, TimeUnit.DAYS);

        assertNotNull("Entry should exist in the cache", cacheStore.get("token:999"));
        assertEquals(entry.getValue(), cacheStore.get("token:999").getValue());
    }

}
