package uk.ac.warwick.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public final class CollectionUtilsTest extends TestCase {
	public void testGroupByProperty() {
		String valueA = "valueA";
		String valueB = "valueB";

		int numberOfValueAs = 3;
		int numberOfValueBs = 4;

		assertFalse(numberOfValueAs == numberOfValueBs); // sanity

		Collection<NestedObject> allAs = new ArrayList<NestedObject>(numberOfValueAs);
		Collection<NestedObject> allBs = new ArrayList<NestedObject>(numberOfValueBs);
		for (int i = 0; i<numberOfValueAs; i++) {
			allAs.add(new NestedObject(new String(valueA)));
		}
		for (int i = 0; i<numberOfValueBs; i++) {
			allBs.add(new NestedObject(new String(valueB)));
		}
		Collection<NestedObject> all = new ArrayList<NestedObject>(numberOfValueAs + numberOfValueBs);
		all.addAll(allAs);
		all.addAll(allBs);

		String propertyName = NestedObject.PROPERTY_NAME;

		Map<String, ?> map = CollectionUtils.groupByProperty(all, propertyName);
		assertEquals("number of groups", 2, map.size());

		assertEquals("valueAs", allAs, map.get(valueA));
		assertEquals("valueBs", allBs, map.get(valueB));
	}
	
	public void testToSetWithSet() {
		Set<String> mySet = new HashSet<String>();
		mySet.add("word");
		assertSame(mySet, CollectionUtils.toSet(mySet));
	}
	
	public void testToSetWithList() {
		List<String> myList = new LinkedList<String>();
		myList.add("one");
		myList.add("two");
		myList.add("three");
		myList.add("two");
		Set<String> theSet = CollectionUtils.toSet(myList);
		assertEquals(3, theSet.size());
		assertTrue(theSet.containsAll(myList));
	}
	
	public void testCountOccurrences() {
		List<String> myList = Arrays.asList(new String[] {
				"twice", "thrice", "twice", "4", "thrice", "thrice", "once",
				"4","4","4"
		});
		Map<String, Integer> results = CollectionUtils.countOccurrences(myList);
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
        List<A> list = new ArrayList<A>();
        list.add(new C());
        list.add(new C());
        list.add(new C());
        list.add(new C());
        list.add(new D());
        list.add(new D());
        list.add(new D());
        list.add(new D());
        
        Collection<C> cList = CollectionUtils.filterByClass(list, C.class);
        
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
    
    public void testBatch() {
    	List<String> list = new ArrayList<String>();
    	
    	for (int i=0; i<150; i++) {
    		list.add(Integer.toString(i));
    	}
    	
    	List<List<String>> batches = CollectionUtils.batch(list, 20);
    	
    	// should have returned 7 batches of 20 and 1 batch of 10
    	assertEquals(8, batches.size());
    	for (int i=0;i<7;i++) {
    		assertEquals(20, batches.get(i).size());
    	}
    	assertEquals(10, batches.get(7).size());
    	
    	for (int i=0;i<7;i++) {
    		for (int j=0;j<20;j++) {
    			int expected = (i*20)+j;
    			assertEquals(Integer.toString(expected), batches.get(i).get(j));
    		}
    	}
    	
    	for (int i=0;i<10;i++) {
    		int expected = 140 + i;
    		assertEquals(Integer.toString(expected), batches.get(7).get(i));
    	}
    }
    
    public void testBatchExact() {
    	List<String> list = new ArrayList<String>();
    	
    	for (int i=0; i<150; i++) {
    		list.add(Integer.toString(i));
    	}
    	
    	List<List<String>> batches = CollectionUtils.batch(list, 50);
    	
    	// should have returned 3 batches of 50
    	assertEquals(3, batches.size());
    	for (List<String> batch : batches) {
    		assertEquals(50, batch.size());
    	}
    }
    
    public void testBigBatchTinyList() {
    	List<String> list = new ArrayList<String>();
    	
    	for (int i=0; i<10; i++) {
    		list.add(Integer.toString(i));
    	}
    	
    	List<List<String>> batches = CollectionUtils.batch(list, 50);
    	
    	// should have returned 1 batch of 10
    	assertEquals(1, batches.size());
    	assertEquals(10, batches.get(0).size());
    }
}
