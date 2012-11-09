package uk.ac.warwick.util.csv;

/**
 * Implementations of this interface will know how to convert a CSV line into the corresponding object.
 *
 * Note: One instance of this class is used for all lines within a CSV.
 *
 * @author xusqac
 */
public interface  CSVLineReader <E> {
    /**
     * Construct a new instance of the object this CSV line is representing.
     */
    E constructNewObject();

    /**
     * Set the specified column with the specified data on the object created in {@see constructNewObject}.
     * @throws CSVException 
     */
    void setColumn(final E obj, final int col, final String data) throws CSVException;

    /**
     * Method which is called after the CSV line has been hydrated.  This method should be used to validate the object.
     * @param obj
     */
    void end(final E obj);
    
    /**
     * Called when all the data has been read. Useful if an implementation wants to do some
     * overall summary calculations on all the data.
     */
    void endData();
}
