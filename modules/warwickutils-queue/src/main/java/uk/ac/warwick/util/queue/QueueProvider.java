package uk.ac.warwick.util.queue;


public interface QueueProvider {
    Queue getQueue(String queueName);
    void destroy();
}