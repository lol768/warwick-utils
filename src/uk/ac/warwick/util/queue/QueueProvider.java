package uk.ac.warwick.util.queue;

import javax.jms.MessageListener;

public interface QueueProvider {

    void send(String queue, String message);

    void destroy();

    void addListener(MessageListener listener, String destinationName);
}