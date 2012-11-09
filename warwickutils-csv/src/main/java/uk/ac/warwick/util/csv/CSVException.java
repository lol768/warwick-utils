package uk.ac.warwick.util.csv;

public final class CSVException extends Exception {

    private static final long serialVersionUID = 4486671398363744254L;

    public CSVException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSVException(String message) {
        super(message);
    }

    public CSVException(Throwable cause) {
        super(cause);
    }

}
