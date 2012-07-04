package uk.ac.warwick.util.hibernate;

/**
 * A wrapper onto ScrollableResults which enforces strong typing and
 * takes a callback to perform an operation. Manages memory effectively within a
 * given batch size.
 */
public interface BatchResults<T> {
    
    interface Callback<T> {
        void run(T entity) throws Exception;
    }
    
    void doWithBatch(Callback<T> callback) throws Exception;
    
}
