package uk.ac.warwick.util.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.Test;
import uk.ac.warwick.util.cache.CacheEntry;

import java.time.Duration;

import static org.junit.Assert.*;

public class CaffeineCacheStoreTest {

    private static final String CACHE_NAME = "customCache";

	@Test
	public void creation() {
		com.github.benmanes.caffeine.cache.Cache<String, CacheEntry<String, String>> caffeineCache = Caffeine.newBuilder().build();
		CaffeineCacheStore<String, String> store = new CaffeineCacheStore<>(CACHE_NAME, caffeineCache);

        CacheEntry<String, String> string = store.get("token:abcdefghij");
		assertNull(string);

		CacheEntry<String, String> entry = new CacheEntry<>("token:12345", "Johnny");
		store.put(entry, Duration.ofSeconds(10));

		assertSame(entry, store.get("token:12345"));
		assertSame(entry, store.get("token:12345"));
		assertEquals(1, store.getStatistics().getCacheSize());

        store.clear();
		assertEquals(0, store.getStatistics().getCacheSize());
	}

	@Test
	public void maxSize() {
		com.github.benmanes.caffeine.cache.Cache<String, CacheEntry<String, String>> caffeineCache = Caffeine.newBuilder().maximumSize(4).build();
		CaffeineCacheStore<String, String> store = new CaffeineCacheStore<>(CACHE_NAME, caffeineCache);

		assertEquals(0, store.getStatistics().getCacheSize());
		store.put(new CacheEntry<>("token:1", "one"), Duration.ofSeconds(10));
		assertEquals(1, store.getStatistics().getCacheSize());
        store.put(new CacheEntry<>("token:2", "two"), Duration.ofSeconds(10));
		assertEquals(2, store.getStatistics().getCacheSize());

        store.put(new CacheEntry<>("token:3", "three"), Duration.ofSeconds(10));
        store.put(new CacheEntry<>("token:4", "three"), Duration.ofSeconds(10));
        store.put(new CacheEntry<>("token:5", "three"), Duration.ofSeconds(10));
        store.put(new CacheEntry<>("token:6", "three"), Duration.ofSeconds(10));
        caffeineCache.cleanUp();
		assertEquals(4, store.getStatistics().getCacheSize());
	}
}
