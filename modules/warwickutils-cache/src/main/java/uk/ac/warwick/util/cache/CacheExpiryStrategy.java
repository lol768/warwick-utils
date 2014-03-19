package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.collections.Pair;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public interface CacheExpiryStrategy<K extends Serializable,V extends Serializable> {
	boolean isExpired(CacheEntry<K,V> entry);
    boolean isStale(CacheEntry<K,V> entry);

    /**
     * Return how long this entry should be cached for. Note that after
     * this time the entry is eligible to be REMOVED from cache, i.e. you won't
     * have a stale copy to do asynchronous updates. You only want to set a time
     * to live if you want the cache system to be allowed to completely sweep away
     * this entry after this time.
     *
     * Return CacheEntryFactory.TIME_TO_LIVE_ETERNITY to never totally expire the value from cache.
     * It will still do asynchronous updates when stale, using the separate expiry time.
     */
    Pair<Number, TimeUnit> getTTL(CacheEntry<K, V> entry);
}
