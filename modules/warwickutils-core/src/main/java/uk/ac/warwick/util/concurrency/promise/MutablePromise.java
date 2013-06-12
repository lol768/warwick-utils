package uk.ac.warwick.util.concurrency.promise;

/**
 * An implementation of a {@link Promise} that acts as a holder for a future
 * value. If the value is null, throws an {@link UnfulfilledPromiseException}.
 */
public class MutablePromise<T> implements Promise<T> {

    private T value;
    
    public MutablePromise() {}

    public MutablePromise(T theValue) {
        setValue(theValue);
    }

    public T fulfilPromise() throws UnfulfilledPromiseException {
        if (value == null) {
            throw new UnfulfilledPromiseException("This promise has not been fulfilled");
        }
        
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }

}
