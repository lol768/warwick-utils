package uk.ac.warwick.util.concurrency.promise;

/**
 * An implementation of a {@link Promise} that is immediately fulfilled.
 */
public final class ImmediatePromise<T> implements Promise<T> {
    
    private final T value;
    
    private ImmediatePromise(T theValue) {
        this.value = theValue;
    }

    public T fulfilPromise() {
        return value;
    }
    
    public static <T> ImmediatePromise<T> of(T value) {
        return new ImmediatePromise<T>(value);
    }

}
