package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.core.DateTimeUtils;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public abstract class TTLCacheExpiryStrategy<K extends Serializable,V extends Serializable> implements CacheExpiryStrategy<K, V> {
    public static <K extends Serializable,V extends Serializable> TTLCacheExpiryStrategy<K, V> eternal() {
        return forTTL(CacheEntryFactory.TIME_TO_LIVE_ETERNITY);
    }

    public static <K extends Serializable,V extends Serializable> TTLCacheExpiryStrategy<K, V> forTTL(Duration ttl) {
        return new TTLCacheExpiryStrategy<K, V>() {
            public Pair<Number, TimeUnit> getTTL(CacheEntry<K, V> entry) {
                if (entry.getValue() != null && entry.getValue().getClass().isAnnotationPresent(CustomCacheExpiry.class)) {
                    return Pair.of(entry.getValue().getClass().getAnnotation(CustomCacheExpiry.class).value(), TimeUnit.MILLISECONDS);
                } else {
                    return Pair.of((ttl.getSeconds() * 1000) + (ttl.getNano() / 1000000), TimeUnit.MILLISECONDS);
                }
            }
        };
    }

    @Override
    public final boolean isExpired(CacheEntry<K, V> entry) {
        // Check if the value class has an annotation for custom cache expiry
        Duration ttl = getTTLDuration(entry);

        if (ttl.equals(CacheEntryFactory.TIME_TO_LIVE_ETERNITY)) {
            return false; // I wanna be the only one to hold you
        }

        final long expires = entry.getTimestamp() + ttl.toMillis();
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
