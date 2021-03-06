package uk.ac.warwick.util.cache.memcached;

import org.junit.Test;
import uk.ac.warwick.util.cache.CacheEntry;

import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MemcachedCacheStoreTest extends AbstractMemcachedCacheStoreTest<String, String> {

    @Test
    public void creation() throws Exception {
        CacheEntry<String, String> string = cacheStore.get("token:abcdefghij");
        assertNull(string);

        CacheEntry<String, String> entry = new CacheEntry<String, String>("token:12345", "Johnny");
        cacheStore.put(entry, Duration.ofSeconds(10));

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

}
