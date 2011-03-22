package uk.ac.warwick.util.queue;

/**
 * Exception type to wrap implementation-specific exceptions thrown
 * by message queue frameworks.
 */
public class MessagingException extends RuntimeException {
    public MessagingException() {
        super();
    }

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingException(Throwable cause) {
        super(cause);
    }
}
