package uk.ac.warwick.util.queue.conversion;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public class AnnotationJsonObjectConverter implements JsonObjectConverter, BeanFactoryAware {

    private final Logger LOGGER = LoggerFactory.getLogger(AnnotationJsonObjectConverter.class);
    
    private Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
    private ObjectMapper objectMapper;
    private AutowireCapableBeanFactory beanFactory;

    public AnnotationJsonObjectConverter() {
        objectMapper = new ObjectMapper();
    }
    
    public Object fromJson(String type, JSONObject json) {
        try {
            Object object = objectMapper.readValue(json.toString(), mappings.get(type));
            
            if (beanFactory != null) {
                wire(object);
            }
            
            return object;
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(e);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void wire(Object object) {
        // Wires based on @Autowire and @Resource annotations; not limited
        // to basic autowiring.
        
        // Collect child objects to wire. Can't autowire as we go as we'll end
        // up recursing into objects we've just wired in.
        List<Object> candidates = new ArrayList<Object>();
        gatherObjects(candidates, object);
        
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Recursively collected " + candidates.size() + " objects to wire");
        for (Object candidate : candidates) {
            beanFactory.autowireBean(candidate);
        }
    }
    
    /**
     * Adds this object to the given list, as well as all non-primitive
     * properties, and all elements/values of Iterable/Map properties.
     */
    private void gatherObjects(List<Object> objs, Object obj) {
        objs.add(obj);
        BeanWrapper wrapper = new BeanWrapperImpl(obj);
        for (PropertyDescriptor prop : wrapper.getPropertyDescriptors()) {
            Class<?> propertyType = prop.getPropertyType();
            if (!propertyType.isPrimitive() 
                    && !propertyType.equals(String.class) 
                    && !"class".equals(prop.getName())) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Found non-primitive field " + prop.getName() + " with type " + propertyType);
                Object value = wrapper.getPropertyValue( prop.getName() );
                if (value != null) {
                    if (propertyType.isArray()) {
                        Object[] array = null;
                        try {
                            array  = (Object[])value;
                        } catch (ClassCastException e) { /* primitive array, skip.*/ }
                        if (array != null) {
                            if (LOGGER.isDebugEnabled()) LOGGER.debug("Looping through array");
                            for (Object o : array) {
                                gatherObjects(objs, o);
                            }
                        }
                    } else if (Iterable.class.isAssignableFrom(propertyType)) {
                        if (LOGGER.isDebugEnabled()) LOGGER.debug("Looping through Iterable");
                        for (Object o : (Iterable)value) {
                            gatherObjects(objs, o);
                        }
                    } else if (Map.class.isAssignableFrom(propertyType)) {
                        // wire values, not keys
                        if (LOGGER.isDebugEnabled()) LOGGER.debug("Looping through Map values");
                        for (Object o : ((Map)value).values()) {
                            gatherObjects(objs, o);
                        }
                    } else {
                        gatherObjects(objs, value);
                    }
                }
            }
        }
    }

    public boolean supports(Object o) {
        return o.getClass().getAnnotation(ItemType.class) != null;
    }

    public JSONObject toJson(Object o) {
        try {
            return new JSONObject(objectMapper.writeValueAsString(o));
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(e);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void registerType(String value, Class<?> c) {
        mappings.put(value, c);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory)beanFactory;
    }

}
