package uk.ac.warwick.util.cache;

import org.junit.Test;

import static org.junit.Assert.*;

public class CacheEntryTest {

	@Test
	public void equality() {
		CacheEntry nullValue1 = new CacheEntry<String, String>("A", null);
		CacheEntry nullValue2 = new CacheEntry<String, String>("A", null);
		CacheEntry valueA = new CacheEntry<>("A", "A");
		CacheEntry valueA2 = new CacheEntry<>("A", "A");
		CacheEntry valueB = new CacheEntry<>("A", "B");

		assertTrue(nullValue1.equals(nullValue2));
		assertTrue(nullValue2.equals(nullValue1));
		assertFalse(nullValue1.equals(valueA));
		assertFalse(valueA.equals(nullValue1));
		assertTrue(valueA.equals(valueA2));
		assertTrue(valueA2.equals(valueA));
		assertFalse(valueB.equals(valueA));
		assertFalse(valueA.equals(valueB));
	}
}
