package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import uk.ac.warwick.util.cache.BasicCache.KeyEntry;

final class UpdateCacheEntryTask<K extends Serializable,V extends Serializable> implements Runnable {

	private final static Logger LOGGER = Logger.getLogger(UpdateCacheEntryTask.class);
	
	private final Collection<KeyEntry<K,V>> entries;
	
	private final BasicCache<K,V> owner;

	public UpdateCacheEntryTask(BasicCache<K,V> cache, KeyEntry<K,V> entry) {
		this(cache, Collections.singletonList(entry));
	}
	
	public UpdateCacheEntryTask(BasicCache<K,V> cache, Collection<KeyEntry<K,V>> entries) {
		super();
		this.owner = cache;
		this.entries = entries;
	}
	
	public static <K extends Serializable,V extends Serializable> UpdateCacheEntryTask<K,V> task(BasicCache<K,V> cache, KeyEntry<K,V> entry) {
		return new UpdateCacheEntryTask<K,V>(cache,entry);
	}
	
	public static <K extends Serializable,V extends Serializable> UpdateCacheEntryTask<K,V> task(BasicCache<K,V> cache, Collection<KeyEntry<K,V>> entry) {
		return new UpdateCacheEntryTask<K,V>(cache,entry);
	}
	
	public void run() {
		try {
			if (entries.size() == 1) {
				owner.updateEntry(entries.iterator().next());
			} else {
				owner.updateEntries(entries);
			}
		} catch (CacheEntryUpdateException e) {
			LOGGER.error("Failed to update entry asynchronously", e);
		}
	}

}
