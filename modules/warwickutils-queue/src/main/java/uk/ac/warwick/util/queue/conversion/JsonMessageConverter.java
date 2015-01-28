package uk.ac.warwick.util.queue.conversion;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import uk.ac.warwick.util.queue.QueueException;

import com.google.common.collect.Maps;

/**
 * Converts objects to and from Json. You can either set a map of ObjectConverters
 * which will specialise in converting each type of object; or you can use 
 * setAnnotatedClasses, giving a list of the classes which have Jackson @Json* annotations
 * plus an @ItemType annotation indicating the type string to attach to serialized messages.
 * <p>
 * If a BeanFactory is wired in, it will assume it is autowire aware, and autowire any
 * bean whose class has @AutowiredOnRehydration on it.
 */
public class JsonMessageConverter implements MessageConverter, BeanFactoryAware, InitializingBean {
   
    private Map<String, JsonObjectConverter> converters = Maps.newHashMap();

    private AutowireCapableBeanFactory beanFactory;

    private AnnotationJsonObjectConverter annotationConverter;

    private ObjectMapper objectMapper;
    
    public Object fromMessage(Message message) throws QueueException {
        try {
            TextMessage string = (TextMessage)message;
            String itemType = string.getStringProperty("itemType");
            //System.out.println("Got itemType" + );
            JSONObject json = new JSONObject(string.getText());
            Object object = converters.get(itemType).fromJson(itemType, json);
            return object;
        } catch (JSONException e) {
            throw new QueueException(e);
        } catch (JMSException e) {
            throw new QueueException(e);
        }
    }
    
    public void setObjectConverters(Map<String, JsonObjectConverter> map) {
        converters.putAll(map);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        if (objectMapper != null && annotationConverter != null) {
            annotationConverter.setObjectMapper(objectMapper);
        }
    }

    public void setAnnotatedClasses(List<Class<?>> annotatedClasses) {
        annotationConverter = new AnnotationJsonObjectConverter();
        if (objectMapper != null) {
            annotationConverter.setObjectMapper(objectMapper);
        }
        for (Class<?> c : annotatedClasses) {
            ItemType itemType = c.getAnnotation(ItemType.class);
            if (itemType == null) {
                throw new IllegalArgumentException("Class " + c + " needs @ItemType annotation to specify a type string");
            }            
            annotationConverter.registerType(itemType.value(), c);
            converters.put(itemType.value(), annotationConverter);
        }
    }

    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        //TODO precalculate/cache class-to-converter mappings
        for (Entry<String,JsonObjectConverter> entry : converters.entrySet()) {
            JsonObjectConverter converter = entry.getValue();
            if (converter.supports(object)) {
                JSONObject json = converter.toJson(object);
                TextMessage textMessage = session.createTextMessage(json.toString());
                
                ItemType itemType = object.getClass().getAnnotation(ItemType.class);
                String type;
                if (itemType != null) {
                    type = itemType.value();
                } else {
                	// This must be a manually added converter, use the entry key
                	type = entry.getKey();
                }
                
                textMessage.setStringProperty("itemType", type);
                return textMessage;
            }
        }
        throw new IllegalArgumentException("No JsonObjectConverter supports this type: " + object.getClass().getName());
    }

    public void setBeanFactory(BeanFactory beans) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory) beans;
    }

    public void afterPropertiesSet() throws Exception {
        if (beanFactory != null && annotationConverter != null) {
            annotationConverter.setBeanFactory(beanFactory);
        }
    }

}
