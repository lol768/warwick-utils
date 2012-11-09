package uk.ac.warwick.util.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class PairIteratorTests extends TestCase {

	final List<String> shorter = Arrays.asList("foo", "bar", "buz");
	final List<String> longer = Arrays.asList("bish", "bosh", "bash", "bang");

	public void testConstructors() {
		new PairIterator<String, String>(
				shorter, longer);
		new PairIterator<String, String>(longer.iterator(), shorter
				.iterator());
		PairIterator.of(shorter, longer)
				.iterator();
	}

	public void testIterate() {
		Iterable<Pair<String, String>> pi = PairIterator.of(shorter, longer);
		int itemsReturned = 0;
		for (Pair<String, String> pair : pi) {
			assertNotNull(pair);
			itemsReturned++;
		}
		assertEquals(longer.size(), itemsReturned);
	}

	public void testCantRemove() {
		Iterable<Pair<String, String>> pi = PairIterator.of(shorter, longer);
		try {
			pi.iterator().remove();
			fail("No exception thrown on remove!");
		} catch (RuntimeException e) {
		}

	}

	public void testIteratePastEndOfOneCollection() {
		Iterable<Pair<String, String>> pi = PairIterator.of(shorter, longer);
		Pair<String, String> pair = skip(pi, 3).iterator().next();
		assertEquals("bang", pair.getRight());
		assertNull(pair.getLeft());
	}
	
	private static <T> Iterable<T> skip(final Iterable<T> iterable, final int skip) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				Iterator<T> iterator = iterable.iterator();
				for (int i=0; i<skip; i++) {
					iterator.next();
				}
				return iterator;
			}
		};
	}
}
