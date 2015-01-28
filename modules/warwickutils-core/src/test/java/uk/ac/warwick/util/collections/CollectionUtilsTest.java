package uk.ac.warwick.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

public final class CollectionUtilsTest extends TestCase {
	public void testGroupByProperty() {
		final String valueA = "valueA";
		final String valueB = "valueB";

		final int numberOfValueAs = 3;
		final int numberOfValueBs = 4;

		assertFalse(numberOfValueAs == numberOfValueBs); // sanity

		final Collection<NestedObject> allAs = new ArrayList<NestedObject>(numberOfValueAs);
		final Collection<NestedObject> allBs = new ArrayList<NestedObject>(numberOfValueBs);
		for (int i = 0; i<numberOfValueAs; i++) {
			allAs.add(new NestedObject(new String(valueA)));
		}
		for (int i = 0; i<numberOfValueBs; i++) {
			allBs.add(new NestedObject(new String(valueB)));
		}
		final Collection<NestedObject> all = new ArrayList<NestedObject>(numberOfValueAs + numberOfValueBs);
		all.addAll(allAs);
		all.addAll(allBs);

		final String propertyName = NestedObject.PROPERTY_NAME;

		final Map<String, ?> map = CollectionUtils.groupByProperty(all, propertyName);
		assertEquals("number of groups", 2, map.size());

		assertEquals("valueAs", allAs, map.get(valueA));
		assertEquals("valueBs", allBs, map.get(valueB));
	}

	public void testToSetWithSet() {
		final Set<String> mySet = new HashSet<String>();
		mySet.add("word");
		assertEquals(mySet, CollectionUtils.toSet(mySet));
	}

	public void testToSetWithList() {
		final List<String> myList = new LinkedList<String>();
		myList.add("one");
		myList.add("two");
		myList.add("three");
		myList.add("two");
		final Set<String> theSet = CollectionUtils.toSet(myList);
		assertEquals(3, theSet.size());
		assertTrue(theSet.containsAll(myList));
	}

	public void testCountOccurrences() {
		final List<String> myList = Arrays.asList(new String[] {
				"twice", "thrice", "twice", "4", "thrice", "thrice", "once",
				"4","4","4"
		});
		final Map<String, Integer> results = CollectionUtils.countOccurrences(myList);
		assertEquals((Integer)1, results.get("once"));
		assertEquals((Integer)2, results.get("twice"));
		assertEquals((Integer)3, results.get("thrice"));
		assertEquals((Integer)4, results.get("4"));
	}

    interface A {
    }

    abstract class B implements A {
    }

    class C extends B implements A {
    }

    class D implements A {
    }

    public void testFilterByClass() {
        final List<A> list = new ArrayList<A>();
        list.add(new C());
        list.add(new C());
        list.add(new C());
        list.add(new C());
        list.add(new D());
        list.add(new D());
        list.add(new D());
        list.add(new D());

        final Collection<C> cList = CollectionUtils.filterByClass(list, C.class);

        assertNotNull(cList);
        assertEquals(cList.size(),4);
    }

    class NestedObject {
        public static final String PROPERTY_NAME="property";
        public final Object object;
        public NestedObject() {
            this.object = null;
        }

        public NestedObject(final Object theObject) {
            this.object = theObject;
        }
        public Object getProperty() {
            return object;
        }
    }

    public void testIterableEnumerable() {
    	final Vector<String> vector = new Vector<String>();
    	vector.add("Hello");
    	vector.add(", ");
    	vector.add("World");

    	final StringBuilder sb = new StringBuilder();
    	for (final String s : CollectionUtils.iterable(vector.elements())) {
    		sb.append(s);
    	}

    	assertEquals("Hello, World", sb.toString());
    }
}
