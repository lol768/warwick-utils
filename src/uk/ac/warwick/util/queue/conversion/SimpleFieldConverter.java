package uk.ac.warwick.util.queue.conversion;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import uk.ac.warwick.util.queue.QueueException;

public class SimpleFieldConverter implements JsonObjectConverter {

    private List<String> fieldsList;
    private final Class<?> targetClazz;
    
    public SimpleFieldConverter(Class<?> clazz, List<String> fields) {
        fieldsList = new ArrayList<String>(fields);
        targetClazz = clazz;
    }

    public Object fromJson(String type, JSONObject json) {
        // ignores the type parameter as this class only handles a single type anyway
        try {
            Object object = BeanUtils.instantiateClass(targetClazz);
            BeanWrapper wrapper = new BeanWrapperImpl(object);
            for (String field : fieldsList) {
                Object value = json.get(field);
                wrapper.setPropertyValue(field, value);
            }
            return object;
        } catch (JSONException e) {
            throw new QueueException(e);
        }
    }

    public JSONObject toJson(Object o) {
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(o);
            JSONObject json = new JSONObject();
            for (String field : fieldsList) {
                Object value = wrapper.getPropertyValue(field);
                json.put(field, value);
            }
            return json;
        } catch (JSONException e) {
            throw new QueueException(e);
        }
    }

    public boolean supports(Object o) {
        return targetClazz.isInstance(o);
    }

}
