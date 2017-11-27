package uk.ac.warwick.util.cache;

import uk.ac.warwick.util.core.DateTimeUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents an entry in the cache. Contains the key and the value.
 * Both must implement Serializable (this isn't mandatory for most of our cache stores
 * but it's better to enforce it now rather than find out later that you can't
 * use a disk-based backend or use any clustering).
 */
public class CacheEntry<K extends Serializable, V extends Serializable> implements Serializable {
	private static final long serialVersionUID = -4384852442875029950L;
	private final K key;
	private final V value;
	private final long created = Instant.now(DateTimeUtils.CLOCK_IMPLEMENTATION).toEpochMilli();
    private transient volatile boolean updating;
	
	@SuppressWarnings("unchecked")
    public CacheEntry(K k, V val) {
	    if (k instanceof String) {
	        // String is final, so it's safe to cast K to String and then back again.
            this.key = (K) new String((String)k);
        } else {
            this.key = k;
        }
	    
		this.value = val;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	public long getTimestamp() {
		return created;
	}

	public boolean isUpdating() {
		return updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().isAssignableFrom(CacheEntry.class)) { return false; }
		
		try {
			CacheEntry<K, V> e = (CacheEntry<K, V>) obj;
			return key.equals(e.key) && Objects.equals(e.value, value);
		} catch (ClassCastException e) {
		    return false;
		}
	}
}
