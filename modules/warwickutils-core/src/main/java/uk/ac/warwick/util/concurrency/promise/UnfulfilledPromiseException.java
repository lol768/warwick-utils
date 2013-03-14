package uk.ac.warwick.util.concurrency.promise;

public class UnfulfilledPromiseException extends Exception {

    private static final long serialVersionUID = 7735282639412135857L;

    public UnfulfilledPromiseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnfulfilledPromiseException(String message) {
        super(message);
    }

    public UnfulfilledPromiseException(Throwable cause) {
        super(cause);
    }

}
