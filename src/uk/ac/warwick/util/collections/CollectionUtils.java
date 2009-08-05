package uk.ac.warwick.util.collections;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**

 */
public final class CollectionUtils {
    private CollectionUtils() {}

    /**
     * <p>Break up the specified collection into groups based on the value of the specified property.</p>
     *
     * <p>For example, if the Collection is of People objects and People has an accessor sex, then the map this returns
     * will contain two collections, one keyed on "male" which will return all males, one called "female".</p>
     *
     * <p>Note: This method will convert the value used as a key to a string by calling the toString method</p>
     * @return Map whose key is the value
     */
    public static <X> SortedMap<String, Collection<X>> groupByProperty(final Collection<X> collection, final String property) {
        return groupByProperty(collection, property, null);
    }

    public static <X> SortedMap<String, Collection<X>> groupByProperty(final Collection<X> collection, final String property, final Comparator<String> comparator) {
        SortedMap<String, Collection<X>> map;
        if (comparator != null) {
        	map = new TreeMap<String, Collection<X>>(comparator);
        } else {
        	map = new TreeMap<String, Collection<X>>();
        }

        final String propertyGetter = "get"+capitalise(property);
        for (final X o: collection) {

        	String value = callGetter(o, propertyGetter);
        	if (value == null) {
        		value = "";
        	}

            Collection<X> group = map.get(value);
            if (group == null) {
                group = new ArrayList<X>();
                map.put(value, group);
            }
            group.add(o);
        }
        return map;
    }

	@SuppressWarnings("unchecked")
	public static <Y, X extends Y> Collection<X> filterByClass(final Collection<Y> collection, final Class<X> clazz) {
        final List<X> results = new ArrayList<X>();

        for (final Y obj : collection) {
            if (clazz.isInstance(obj)) {
                results.add((X)obj);
            }
        }

        return results;
    }

	/**
	 * Converts a collection to a set, or returns the same object if the
	 * collection is a set.
	 * Obviously if the collection has any duplicate elements, at least
	 * one of these will be lost in the returned Set.
	 */
	@SuppressWarnings("unchecked")
	public static <E> Set<E> toSet(final Collection<E> collection) {
		if (collection instanceof Set) {
			return (Set<E>)collection;
		}
		
		final Set<E> set = new HashSet<E>();
		set.addAll(collection);
		return set;
	}

	/**
	 * Given a Collection of elements, returns a map mapping elements to
	 * the number of times that element was found (according to E's definition
	 * of equals() as used by Map.containsKey()).
	 */
	public static <E> Map<E, Integer> countOccurrences(final Collection<E> collection) {
		final Map<E, Integer> map = new HashMap<E, Integer>();
		for (final E item : collection) {
			if (map.containsKey(item)) {
				map.put(item, map.get(item).intValue() + 1);
			} else {
				map.put(item, 1);
			}
		}
		return map;
	}

	/**
	 * Join two string arrays into one.
	 *
	 * (I tried to make a generic method but you can't create a generic-type
	 * array at runtime.)
	 */
	public static String[] joinArrays(final String[] oneArray, final String[] twoArray) {
		final String[] result = new String[oneArray.length + twoArray.length];
        System.arraycopy(oneArray, 0, result, 0, oneArray.length);
        System.arraycopy(twoArray, 0, result, oneArray.length, twoArray.length);
        return result;
	}

	public static <T> List<T> splice(final List<T> list, final int num, final int skip) {
	    if (list.isEmpty()) {
	        return list;
	    }

        int fromIndex = skip;

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (fromIndex > list.size() - 1) {
            fromIndex = list.size() - 1;
        }

        // splice
        int toIndex = num + fromIndex;
        if (toIndex > list.size()) {
            toIndex = list.size();
        }

        if (fromIndex > toIndex) {
            toIndex = fromIndex;
        }

        return list.subList(fromIndex, toIndex);
    }

	public static <T> List<List<T>> batch(final List<T> list, final int batchSize) {
		final List<List<T>> result = new ArrayList<List<T>>();

		for (int i=0; i < list.size(); i+=batchSize) {
			result.add(list.subList(i, Math.min(i+batchSize, list.size())));
		}

		return result;
	}

	/**
	 * Returns an Iterable wrapping the given Enumeration. Most useful for using
	 * Enumerations in a new-style for loop.
	 * <pre>
	 * for (String name : CollectionUtils.iterable(myVector.elements()) { ... }
	 * </pre>
	 */
	public static <T> Iterable<T> iterable(final Enumeration<T> enumeration) {
		return new IterableEnumeration<T>(enumeration);
	}

	/**
     * Calls bean.{getterMethod}() and returns a value as a String
     */
	private static String callGetter(final Object bean, final String getterMethod) {
		try {
			final Class<?> clazz = bean.getClass();
			final Method method = clazz.getMethod(getterMethod, new Class[0]);
			final Object value = method.invoke(bean, new Object[0]);
			return value==null? null : value.toString();
		} catch (final Exception e) {
			throw new IllegalArgumentException("Failed to get value of bean property", e);
		}
	}

    private static String capitalise(final String property) {
		final char[] cs = property.toCharArray();
		cs[0] = Character.toUpperCase(cs[0]);
		return new String(cs);
	}

    private static class IterableEnumeration<T> implements Iterable<T>, Iterator<T> {
    	private final Enumeration<T> enumeration;
		IterableEnumeration(final Enumeration<T> e) {
    		this.enumeration = e;
    	}
		public Iterator<T> iterator() {
			return this;
		}
		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}
		public T next() {
			return enumeration.nextElement();
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
    }
}
