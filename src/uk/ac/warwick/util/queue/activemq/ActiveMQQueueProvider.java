package uk.ac.warwick.util.queue.activemq;

import static org.springframework.util.StringUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import uk.ac.warwick.util.queue.Queue;
import uk.ac.warwick.util.queue.QueueListener;
import uk.ac.warwick.util.queue.QueueProvider;

public class ActiveMQQueueProvider implements DisposableBean, QueueProvider {
    
    private ActiveMQConnectionFactory connectionFactory;
    private CachingConnectionFactory cachingConnectionFactory;
    
    //private List<AbstractJmsListeningContainer> listenerContainers = new ArrayList<AbstractJmsListeningContainer>();
    
    private Map<String, NativeQueue> queues = new HashMap<String, NativeQueue>();
    
    /**
     * @param brokerURL URL of the ActiveMQ server, e.g. tcp://127.0.0.1:61616
     */
    public ActiveMQQueueProvider(String brokerURL) {        
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
    }
    
    public ActiveMQQueueProvider(String brokerURL, String username, String password) {
        this(brokerURL);
        if (hasText(username) && hasText(password)) {
            connectionFactory.setUserName(username);
            connectionFactory.setPassword(password);
        }
    }
    
    /**
     * Returns an embedded ActiveMQQueueProvider with persistence disabled - 
     * useful for testing. You should still make sure to call the usual lifecycle
     * methods on this.
     */
    public static ActiveMQQueueProvider createEmbeddedBroker() {
        return new ActiveMQQueueProvider("vm://embedded?broker.persistent=false&broker.useJmx=false");
    }

//    public void setListeners(Map<MessageListener, String> mappings) {
//        if (!listenerContainers.isEmpty()) {
//            throw new IllegalStateException("Can't use setListeners after some listeners have already been added");
//        }
//        for (Entry<MessageListener,String> mapping : mappings.entrySet()) {
//            addListener(mapping.getKey(), mapping.getValue());
//        }
//    }

    public void destroy() {
        /*
         * Need to destroy all the queues, which will destroy
         * all the listener containers, before we can destroy
         * the connection factory. Otherwise there will be errors
         * as things will try to do stuff with messages during
         * shutdown.
         */
        for (NativeQueue queue : queues.values()) {
            queue.destroy();
        }
        cachingConnectionFactory.destroy();
    }

    public Queue getQueue(String queueName) {
        NativeQueue queue = new NativeQueue(queueName);
        queues.put(queueName, queue);
        return queue;
    }
    
    class NativeQueue implements Queue, DisposableBean {
        //private ActiveMQQueue q;
        private JmsTemplate jms;
        private String name;
        private List<DefaultMessageListenerContainer> containers = new ArrayList<DefaultMessageListenerContainer>();
        
        public NativeQueue(String name) {
            this.name = name;
            //q = new ActiveMQQueue(name);
            jms = new JmsTemplate(cachingConnectionFactory);
        }
        
        public void addListener(String itemType, final QueueListener listener) {
            if (!listener.isListeningToQueue()) {
                return;
            }
            DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
            container.setSessionTransacted(true);
            container.setConnectionFactory(cachingConnectionFactory);
            container.setDestinationName(name);
            // Selector is an SQL92 type condition to pick which types of messages to receive
            if (itemType != null) {
                container.setMessageSelector("itemType = '" + itemType.replace("'", "''") + "'");
            }
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {
                        listener.onReceive( jms.getMessageConverter().fromMessage(message) );
                    } catch (MessageConversionException e) {
                        throw new IllegalStateException(e);
                    } catch (JMSException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
            container.afterPropertiesSet();
            containers.add(container);
        }

        public void send(Object message) {
            jms.convertAndSend(name, message);
        }

        public void setPersistent(boolean persistent) {
            jms.setDeliveryPersistent(persistent);
        }

        public void destroy() {
            for (DefaultMessageListenerContainer container : containers) {
                container.destroy();
            }
        }

        public void setMessageConverter(MessageConverter converter) {
            jms.setMessageConverter(converter);
        }

        public void setSingleListener(QueueListener listener) {
            if (!containers.isEmpty()) {
                throw new IllegalStateException("Can only set a single listener if no other listeners have been set on this queue");
            }
            addListener(null, listener);
        }
        
    }

}
