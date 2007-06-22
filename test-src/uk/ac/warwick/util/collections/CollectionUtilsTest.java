package uk.ac.warwick.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

		Map map = CollectionUtils.groupByProperty(all, propertyName);
		assertEquals("number of groups", 2, map.size());

		assertEquals("valueAs", allAs, map.get(valueA));
		assertEquals("valueBs", allBs, map.get(valueB));
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
}
