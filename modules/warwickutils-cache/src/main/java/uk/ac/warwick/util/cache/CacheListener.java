package uk.ac.warwick.util.cache;

import java.io.Serializable;

public interface CacheListener<K extends Serializable,V extends Serializable> {
    void cacheMiss(final K key, final CacheEntry<K, V> newEntry);
    void cacheHit(final K key, final CacheEntry<K, V> entry);
}
