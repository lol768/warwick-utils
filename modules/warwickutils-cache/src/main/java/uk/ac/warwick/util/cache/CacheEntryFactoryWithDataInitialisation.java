package uk.ac.warwick.util.cache;

import java.io.Serializable;

public abstract class CacheEntryFactoryWithDataInitialisation<K extends Serializable,V extends Serializable, T> implements CacheEntryFactory<K, V> {

    public abstract V create(K key, T data) throws CacheEntryUpdateException;

    public final V create(K key) throws CacheEntryUpdateException {
        return create(key, null);
    }

}
