package uk.ac.warwick.util.cache.memcached;

import org.junit.Test;
import uk.ac.warwick.util.cache.CacheEntry;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MemcachedCacheStoreLongTTLTest extends AbstractMemcachedCacheStoreTest<String, String> {

    @Test
    public void longTTLs() {
        // UTL-212
        CacheEntry<String, String> entry = new CacheEntry<>("token:999", "Paul");
        cacheStore.put(entry, Duration.ofDays(31));

        assertNotNull("Entry should exist in the cache", cacheStore.get("token:999"));
        assertEquals(entry.getValue(), cacheStore.get("token:999").getValue());
    }

}
