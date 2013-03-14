package uk.ac.warwick.util.concurrency.promise;

public abstract class Promises {
    
    private Promises() {}
    
    /**
     * Helper function that returns a fulfilled value, or null if the promise
     * hasn't been fulfilled yet.
     */
    public static final <T> T fulfilOrNull(Promise<T> promise) {
        try {
            return promise.fulfilPromise();
        } catch (UnfulfilledPromiseException e) {
            // Promise has not been fulfilled yet
            return null;
        }
    }

}
