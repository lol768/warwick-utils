package uk.ac.warwick.util.cache.memcached;

import org.junit.Test;
import uk.ac.warwick.util.cache.CacheEntry;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MemcachedCacheStoreLongTTLTest extends AbstractMemcachedCacheStoreTest<String, String> {

    @Test
    public void longTTLs() throws Exception {
        // UTL-212
        CacheEntry<String, String> entry = new CacheEntry<>("token:999", "Paul");
        cacheStore.put(entry, 31, TimeUnit.DAYS);

        assertNotNull("Entry should exist in the cache", cacheStore.get("token:999"));
        assertEquals(entry.getValue(), cacheStore.get("token:999").getValue());
    }

}
