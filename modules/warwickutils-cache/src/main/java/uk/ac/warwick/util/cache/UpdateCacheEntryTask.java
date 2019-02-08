package uk.ac.warwick.util.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.util.cache.BasicCache.KeyEntry;

final class UpdateCacheEntryTask<K extends Serializable,V extends Serializable,T> implements Runnable {

	private final static Logger LOGGER = LoggerFactory.getLogger(UpdateCacheEntryTask.class);
	
	private final Collection<KeyEntry<K, V, T>> entries;
	
	private final BasicCache<K, V, T> owner;

	public UpdateCacheEntryTask(BasicCache<K, V, T> cache, KeyEntry<K, V, T> entry) {
		this(cache, Collections.singletonList(entry));
	}
	
	public UpdateCacheEntryTask(BasicCache<K, V, T> cache, Collection<KeyEntry<K, V, T>> entries) {
		super();
		this.owner = cache;
		this.entries = entries;
	}
	
	public static <K extends Serializable,V extends Serializable,T> UpdateCacheEntryTask<K, V, T> task(BasicCache<K, V, T> cache, KeyEntry<K, V, T> entry) {
		return new UpdateCacheEntryTask<K, V, T>(cache,entry);
	}
	
	public static <K extends Serializable,V extends Serializable,T> UpdateCacheEntryTask<K, V, T> task(BasicCache<K, V, T> cache, Collection<KeyEntry<K, V, T>> entry) {
		return new UpdateCacheEntryTask<K, V, T>(cache,entry);
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
