package uk.ac.warwick.util.core;

/**
 * Factory interface. (which for some reason is not called Factory.)
 */
public interface ObjectProvider<T> {
    
    T newInstance();

}
