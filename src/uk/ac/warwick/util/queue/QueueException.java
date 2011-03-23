package uk.ac.warwick.util.queue;

/**
 * Exception type to wrap implementation-specific exceptions thrown
 * by message queue frameworks.
 */
public class QueueException extends RuntimeException {
    public QueueException() {
        super();
    }

    public QueueException(String message) {
        super(message);
    }

    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueException(Throwable cause) {
        super(cause);
    }
}
