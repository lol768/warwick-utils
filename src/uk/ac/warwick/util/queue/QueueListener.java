package uk.ac.warwick.util.queue;

/**
 * Implemented by an object who wants to consume queue items.
 */
public interface QueueListener {
    void onReceive(Object object);
}
