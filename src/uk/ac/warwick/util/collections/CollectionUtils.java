package uk.ac.warwick.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

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
        for (X o: collection) {
            BeanWrapper wrapper = new BeanWrapperImpl(o);
            String value = wrapper.getPropertyValue(property).toString();
            Collection<X> group = map.get(value);
            if (group == null) {
                group = new ArrayList<X>();
                map.put(value, group);
            }
            group.add(o);
        }
        return map;
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
