package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * EntryFactory which doesn't support batch lookups.
 */
public abstract class SingularCacheEntryFactoryWithDataInitialisation<K extends Serializable, V extends Serializable, T>
    extends CacheEntryFactoryWithDataInitialisation<K, V, T> {

    public final boolean isSupportsMultiLookups() {
        return false;
    }

    public final Map<K,V> create(List<K> keys) throws CacheEntryUpdateException {
        throw new UnsupportedOperationException();
    }

}
