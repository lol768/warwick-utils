package uk.ac.warwick.util.cache.memcached;

import org.junit.*;
import uk.ac.warwick.util.cache.CacheEntry;

import java.util.Map;

import static org.junit.Assert.*;

public class MemcachedCacheStoreTest extends AbstractMemcachedCacheStoreTest<String, String> {

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

}
