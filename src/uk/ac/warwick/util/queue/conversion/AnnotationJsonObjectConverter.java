package uk.ac.warwick.util.queue.conversion;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import uk.ac.warwick.util.hibernate.annotations.AutowiredOnRehydration;

public class AnnotationJsonObjectConverter implements JsonObjectConverter, BeanFactoryAware {

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
                // Wires based on @Autowire and @Resource annotations; not limited
                // to basic autowiring.
                beanFactory.autowireBean(object);
            }
            
            return object;
        } catch (JsonGenerationException e) {
            throw new IllegalArgumentException(e);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean supports(Object o) {
        return o.getClass().getAnnotation(ItemType.class) != null;
    }

    public JSONObject toJson(Object o) {
        try {
            return new JSONObject(objectMapper.writeValueAsString(o));
        } catch (JsonGenerationException e) {
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

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory)beanFactory;
    }

}
