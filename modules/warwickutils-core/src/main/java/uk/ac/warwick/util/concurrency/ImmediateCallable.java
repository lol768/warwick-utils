package uk.ac.warwick.util.concurrency;

import java.util.concurrent.Callable;

public final class ImmediateCallable {
    private ImmediateCallable() {}

    public static <T> Callable<T> newInstance(final T value) {
        return new Callable<T>() {
            public T call() throws Exception {
                return value;
            }  
        };
    }
    
    public static <T> Callable<T> errorInstance(final Exception error) {
        return new Callable<T>() {
            public T call() throws Exception {
                throw error;
            }  
        };
    }

}
