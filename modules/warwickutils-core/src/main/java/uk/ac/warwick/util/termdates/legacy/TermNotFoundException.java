package uk.ac.warwick.util.termdates.legacy;

/**
 * @deprecated Use {@link uk.ac.warwick.util.termdates.AcademicYear}
 */
public class TermNotFoundException extends Exception {

    private static final long serialVersionUID = -2486925863691049004L;
    
    public TermNotFoundException(final String message) {
        super(message);
    }

}
