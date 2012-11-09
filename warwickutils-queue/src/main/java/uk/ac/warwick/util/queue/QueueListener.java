package uk.ac.warwick.util.queue;

/**
 * Implemented by an object who wants to consume queue items.
 */
public interface QueueListener {
    void onReceive(Object object);
    
    /**
     * If this returns false, the Queue should not bother adding it as a listener.
     * While it seems odd to add a listener when you don't want it to listen,
     * it makes Spring configuration easier for scheduler-only consumers. 
     */
    boolean isListeningToQueue();
}
