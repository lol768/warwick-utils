package uk.ac.warwick.util.queue;

import org.springframework.jms.support.converter.MessageConverter;

public interface Queue {
    /**
     * Set whether messages sent to this queue should be persistent.
     * Default is true.
     */
    void setPersistent(boolean persistent);
    
    void send(Object message);
    
    void setMessageConverter(MessageConverter converter);
    
    /**
     * @param itemType Item type as defined in the message converter. Pass null to receive all messages for this queue.
     * @param listener The listener to receive the objects.
     */
    void addListener(String itemType, QueueListener listener);

    /**
     * Set one listener which will listen to all messages on this queue. Useful if you already
     * have stuff set up to do the right thing with each incoming object type.
     */
    void setSingleListener(QueueListener listener);
}
