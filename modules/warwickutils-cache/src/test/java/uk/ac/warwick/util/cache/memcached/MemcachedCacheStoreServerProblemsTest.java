package uk.ac.warwick.util.cache.memcached;

import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.cache.BasicCache;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.cache.TTLCacheExpiryStrategy;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class MemcachedCacheStoreServerProblemsTest extends AbstractMemcachedCacheStoreTest<String, String> {

    private int cacheCallCount = 0;

    // Use an actual cache, rather than going to the store directly
    private BasicCache<String, String, Object> cache;

    @Before
    public void setUpCache() {
        cache = new BasicCache<>(cacheStore, Caches.wrapFactoryWithoutDataInitialisation(new SingularCacheEntryFactory<String, String>() {
            public String create(String key) {
                cacheCallCount++;
                return key.substring(6);
            }

            public boolean shouldBeCached(String val) {
                return true;
            }
        }), TTLCacheExpiryStrategy.forTTL(Duration.ofSeconds(10)), false, false);
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

        // FIXME This should all work but doesn't on Bamboo
//
//        // Phew
//        daemon.start();
//
//        Thread.sleep(1000);
//
//        // Wait for a reconnect
//        for (int i = 0; i < DefaultConnectionFactory.DEFAULT_MAX_RECONNECT_DELAY * 10; i++) {
//            try {
//                if (client.getStats() != null && cacheStore.getStatistics().getCacheSize() == 0) {
//                    break;
//                }
//                Thread.sleep(100);
//            } catch (Exception e) {
//                // do nothing
//            }
//        }
//
//        assertSize(0);
//
//        assertEquals("12345", cache.get("token:12345"));
//        assertEquals("12345", cache.get("token:12345"));
//        assertSize(1);
//        assertEquals(3, cacheCallCount);
    }
}
