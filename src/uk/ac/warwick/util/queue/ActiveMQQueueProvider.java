package uk.ac.warwick.util.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class ActiveMQQueueProvider implements DisposableBean, QueueProvider {
    private JmsTemplate jms;
    private ActiveMQConnectionFactory connectionFactory;
    private CachingConnectionFactory cachingConnectionFactory;
    
    private List<AbstractJmsListeningContainer> listenerContainers = new ArrayList<AbstractJmsListeningContainer>();
    
    /**
     * @param brokerURL URL of the ActiveMQ server, e.g. tcp://127.0.0.1:61616
     */
    public ActiveMQQueueProvider(String brokerURL) {        
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
        jms = new JmsTemplate(cachingConnectionFactory);
    }
    
    /**
     * Returns an embedded ActiveMQQueueProvider with persistence disabled - 
     * useful for testing. You should still make sure to call the usual lifecycle
     * methods on this.
     */
    public static ActiveMQQueueProvider createEmbeddedBroker() {
        return new ActiveMQQueueProvider("vm://embedded?broker.persistent=false");
    }
    
    public void send(String queue, String message) {
        try {
            jms.convertAndSend(queue, message);
        } catch (JmsException e) {
            throw new MessagingException(e);
        }
    }
        
    public void addListener(MessageListener listener, String destinationName) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(cachingConnectionFactory);
        container.setDestinationName(destinationName);
        container.setMessageListener(listener);
        container.afterPropertiesSet();
        listenerContainers.add(container);
    }
    
    public void setListeners(Map<MessageListener, String> mappings) {
        if (!listenerContainers.isEmpty()) {
            throw new IllegalStateException("Can't use setListeners after some listeners have already been added");
        }
        for (Entry<MessageListener,String> mapping : mappings.entrySet()) {
            addListener(mapping.getKey(), mapping.getValue());
        }
    }
    
    /* (non-Javadoc)
     * @see uk.ac.warwick.util.queue.QueueProvider#destroy()
     */
    public void destroy() {
        for (AbstractJmsListeningContainer c : listenerContainers) {
            c.destroy();
        }
        cachingConnectionFactory.destroy();
    }
}
