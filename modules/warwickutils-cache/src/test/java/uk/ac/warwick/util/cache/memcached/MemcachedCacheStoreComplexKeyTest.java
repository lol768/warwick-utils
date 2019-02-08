package uk.ac.warwick.util.cache.memcached;

import org.junit.Test;
import uk.ac.warwick.util.cache.CacheEntry;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MemcachedCacheStoreComplexKeyTest extends AbstractMemcachedCacheStoreTest<MemcachedCacheStoreComplexKeyTest.ComplexKey, MemcachedCacheStoreComplexKeyTest.ComplexValue> {

    @Test
    public void complexKey() throws Exception {
        CacheEntry<ComplexKey, ComplexValue> string = cacheStore.get(new ComplexKey("token:abcdefghij"));
        assertNull(string);

        CacheEntry<ComplexKey, ComplexValue> entry = new CacheEntry<ComplexKey, ComplexValue>(new ComplexKey("token:12345"), new ComplexValue("Johnny"));
        cacheStore.put(entry, Duration.ofSeconds(10));

        assertEquals(entry.getValue(), cacheStore.get(new ComplexKey("token:12345")).getValue());
        assertEquals(entry.getValue(), cacheStore.get(new ComplexKey("token:12345")).getValue());

        // Check the cacheStore stats are right
        assertSize(1);

        Map<String, String> stats = client.getStats().values().iterator().next();
        assertEquals("2", stats.get("get_hits"));
        assertEquals("1", stats.get("get_misses"));

        cacheStore.clear();
        assertSize(0);
    }

    static final class ComplexKey implements Serializable {

        private final String baseKey;

        ComplexKey(String baseKey) {
            this.baseKey = baseKey;
        }

        public String getBaseKey() {
            return baseKey;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ComplexKey && ((ComplexKey)obj).getBaseKey().equals(getBaseKey());
        }

        @Override
        public int hashCode() {
            return getBaseKey().hashCode();
        }
    }

    static final class ComplexValue implements Serializable {

        private final String baseValue;

        ComplexValue(String baseValue) {
            this.baseValue = baseValue;
        }

        public String getBaseValue() {
            return baseValue;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ComplexValue && ((ComplexValue)obj).getBaseValue().equals(getBaseValue());
        }

        @Override
        public int hashCode() {
            return getBaseValue().hashCode();
        }

    }

}
