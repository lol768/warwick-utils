package uk.ac.warwick.util.cache;

import java.io.Serializable;

public interface CacheWithDataInitialisation<K extends Serializable, V extends Serializable, T> extends Cache<K, V> {

    V get(final K key, final T data) throws CacheEntryUpdateException;

    Result<V> getResult(final K key, final T data) throws CacheEntryUpdateException;

}
