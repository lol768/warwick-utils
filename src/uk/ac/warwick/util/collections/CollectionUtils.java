package uk.ac.warwick.util.collections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton utility class.
 *
 * @author xusqac
 */
public final class CollectionUtils {
    private CollectionUtils() {

    }

    /**
     * <p>Break up the specified collection into groups based on the value of the specified property.</p>
     *
     * <p>For example, if the Collection is of People objects and People has an accessor sex, then the map this returns
     * will contain two collections, one keyed on "male" which will return all males, one called "female".</p>
     *
     * <p>Note: This method will convert the value used as a key to a string by calling the toString method</p>
     * @return Map whose key is the value
     */
    public static <X> Map<String, Collection<X>> groupByProperty(final Collection<X> collection, final String property) {
        Map<String, Collection<X>> map = new HashMap<String, Collection<X>>();
        String propertyGetter = "get"+capitalise(property);
        for (X o: collection) {

        	String value = callGetter(o, propertyGetter);
        	
            Collection<X> group = map.get(value);
            if (group == null) {
                group = new ArrayList<X>();
                map.put(value, group);
            }
            group.add(o);
        }
        return map;
    }

    /**
     * Calls bean.{getterMethod}() and returns a value as a String
     */
	private static <X> String callGetter(X bean, String getterMethod) {
		try {
			Class clazz = bean.getClass();
			Method method = clazz.getMethod(getterMethod, new Class[0]);
			Object value = method.invoke(bean, new Object[0]);
			return value==null? null : value.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to get value of bean property", e);
		}
	}
    
    private static String capitalise(String property) {
		char[] cs = property.toCharArray();
		cs[0] = Character.toUpperCase(cs[0]);
		return new String(cs);
	}

	public static <Y, X extends Y> Collection<X> filterByClass(Collection<Y> collection, Class<X> clazz) {
        List<X> results = new ArrayList<X>();
        
        for (Y obj : collection) {
            if (clazz.isInstance(obj)) {
                results.add((X)obj);
            }
        }
        
        return results;
    }
}
