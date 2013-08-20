package uk.ac.warwick.util.hibernate;

/**
 * For backwards compatibility with warwickutils-files, which
 * references this interface.
 */
public interface BatchResults<T> {

    interface Callback<T> {
        void run(T entity) throws Exception;
    }

    void doWithBatch(Callback<T> callback) throws Exception;

}
