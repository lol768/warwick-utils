package uk.ac.warwick.util.cache;

import java.io.Serializable;

public interface CacheExpiryStrategy<K extends Serializable,V extends Serializable> {
	boolean isExpired(CacheEntry<K,V> entry);
}
