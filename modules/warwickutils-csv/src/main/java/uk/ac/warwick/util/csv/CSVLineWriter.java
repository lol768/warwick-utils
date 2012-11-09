package uk.ac.warwick.util.csv;

/**
 * Implementations of this interface will know how to convert an object into a CSV line.
 *
 * @author xusqac
 */
public interface CSVLineWriter<T> {
    int getNoOfColumns(final T o);
    String getColumn(final T o, final int col);
}
