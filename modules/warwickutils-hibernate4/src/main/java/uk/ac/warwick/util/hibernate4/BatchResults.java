package uk.ac.warwick.util.hibernate4;

/**
 * A wrapper onto ScrollableResults which enforces strong typing and
 * takes a callback to perform an operation. Manages memory effectively within a
 * given batch size.
 */
public interface BatchResults<T> extends uk.ac.warwick.util.hibernate.BatchResults<T> {}
