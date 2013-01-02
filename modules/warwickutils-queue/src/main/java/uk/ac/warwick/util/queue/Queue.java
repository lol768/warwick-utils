package uk.ac.warwick.util.queue;

import org.springframework.jms.support.converter.MessageConverter;

public interface Queue {
	/**
	 * Set whether messages sent to this queue should be persistent. Default is
	 * true.
	 */
	void setPersistent(boolean persistent);

	/**
	 * Set whether messages sent to this "queue" are broadcast into the pub sub
	 * domain, rather than being one-in, one-out.
	 * 
	 * TODO Strictly speaking, this effectively means that it's not a Queue at
	 * all, it's a Topic.
	 */
	void setPubSub(boolean pubSub);
	
	/**
	 * Set this to true to inhibit delivery of messages published by its own
	 * connection when in pubsub mode. Default is false, i.e. it will receive
	 * its own messages.
	 */
	void setPubSubNoLocal(boolean pubSubNoLocal);

	void send(Object message);

	void setMessageConverter(MessageConverter converter);

	/**
	 * @param itemType
	 *            Item type as defined in the message converter. Pass null to
	 *            receive all messages for this queue.
	 * @param listener
	 *            The listener to receive the objects.
	 */
	void addListener(String itemType, QueueListener listener);

	/**
	 * Set one listener which will listen to all messages on this queue. Useful
	 * if you already have stuff set up to do the right thing with each incoming
	 * object type.
	 */
	void setSingleListener(QueueListener listener);
}
