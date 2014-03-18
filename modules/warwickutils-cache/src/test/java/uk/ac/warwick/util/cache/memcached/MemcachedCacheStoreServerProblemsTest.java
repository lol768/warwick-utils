package uk.ac.warwick.util.cache.memcached;

import net.spy.memcached.DefaultConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.cache.BasicCache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;

import static org.junit.Assert.assertEquals;

public class MemcachedCacheStoreServerProblemsTest extends AbstractMemcachedCacheStoreTest<String, String> {

    private int cacheCallCount = 0;

    // Use an actual cache, rather than going to the store directly
    private BasicCache<String, String> cache;

    @Before
    public void setUpCache() throws Exception {
        cache  = new BasicCache<String, String>(cacheStore, new SingularCacheEntryFactory<String, String>() {
            public String create(String key) throws CacheEntryUpdateException {
                cacheCallCount++;
                return key.substring(6);
            }

            public boolean shouldBeCached(String val) {
                return true;
            }
        }, 10);
    }

    @Test
    public void handlesUnavailableServerGracefully() throws Exception {
        assertEquals("12345", cache.get("token:12345"));
        assertSize(1);
        assertEquals(1, cacheCallCount);

        // Puppet has run, installed new config that I didn't want and the service has segfaulted
        daemon.stop();

        assertEquals("12345", cache.get("token:12345"));
        assertEquals(2, cacheCallCount);

        // Phew
        daemon.start();

        // Wait for a reconnect
        for (int i = 0; i < DefaultConnectionFactory.DEFAULT_MAX_RECONNECT_DELAY * 10; i++) {
            try {
                if (client.getStats() != null && cacheStore.getStatistics().getCacheSize() == 0) {
                    break;
                }
                Thread.sleep(100);
            } catch (Exception e) {
                // do nothing
            }
        }

        assertSize(0);

        assertEquals("12345", cache.get("token:12345"));
        assertEquals("12345", cache.get("token:12345"));
        assertSize(1);
        assertEquals(3, cacheCallCount);
    }
}
