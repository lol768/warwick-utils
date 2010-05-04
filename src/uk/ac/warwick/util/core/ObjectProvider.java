package uk.ac.warwick.util.core;

/**
 * Factory interface.
 */
public interface ObjectProvider<T> {
    
    T newInstance();

}
