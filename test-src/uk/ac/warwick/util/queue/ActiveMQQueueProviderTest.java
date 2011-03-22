package uk.ac.warwick.util.queue;

import static org.junit.Assert.*;

import java.io.File;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;


public class ActiveMQQueueProviderTest {
    
    private QueueProvider queue;

    @Before public void create() {
        queue = ActiveMQQueueProvider.createEmbeddedBroker();
    }
    
    @Test public void addMessage() throws Exception {        
        MyListener listener = new MyListener();
        queue.addListener(listener, "Test.Messages");
        
        queue.send("Test.Messages", "Hello from JUnit");
        Thread.sleep(500);
        
        assertEquals(1, listener.getMessagesReceived());
    }
    
    @After public void destroy() {        
        queue.destroy();
    }
    
    @AfterClass public static void checkEmbeddedProvider() {
        assertFalse( "Embedded queue shouldn't create activemq-data directory", 
                new File("activemq-data").exists() );
    }
    
    public static class MyListener implements MessageListener {
        private int messagesReceived;
        public void onMessage(Message message) {
            if (message instanceof TextMessage) {
                try {
                    messagesReceived++;
                    System.out.println(((TextMessage) message).getText());
                }
                catch (JMSException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else {
                throw new IllegalArgumentException("Message must be of type TextMessage");
            }
        }
        public int getMessagesReceived() {
            return messagesReceived;
        }
        
    }
}
