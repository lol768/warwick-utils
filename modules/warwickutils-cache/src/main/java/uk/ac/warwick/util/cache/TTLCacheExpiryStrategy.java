package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.core.DateTimeUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public abstract class TTLCacheExpiryStrategy<K extends Serializable,V extends Serializable> implements CacheExpiryStrategy<K, V> {
    @Override
    public final boolean isExpired(CacheEntry<K,V> entry) {
        // Check if the value class has an annotation for custom cache expiry
        Pair<Number, TimeUnit> ttl = getTTL(entry);

        if (ttl.getLeft().intValue() == CacheEntryFactory.TIME_TO_LIVE_ETERNITY) {
            return false; // I wanna be the only one to hold you
        }

        final long expires = entry.getTimestamp() + ttl.getRight().toMillis(ttl.getLeft().longValue());
        final long now = Instant.now(DateTimeUtils.CLOCK_IMPLEMENTATION).toEpochMilli();
        return expires <= now;
    }

    @Override
    public boolean isStale(CacheEntry<K, V> entry) {
        return isExpired(entry);
    }

    @Override
    public abstract Pair<Number, TimeUnit> getTTL(CacheEntry<K, V> entry);
}
