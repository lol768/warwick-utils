package uk.ac.warwick.util.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

public final class ExceptionUtils {
	
    private ExceptionUtils() {
    }

    public static Throwable retrieveException(final Exception e, final Class<? extends Throwable> clazz) {
        for (Throwable t: retrieveExceptions(e)) {
            if (t.getClass().equals(clazz)) {
                return t;
            }
        }

        return null;
    }

    public static List<Throwable> retrieveExceptions(final Exception e) {
        List<Throwable> exceptions = new ArrayList<Throwable>();
        walkExceptions(exceptions, e);
        return exceptions;
    }

    private static void walkExceptions(final List<Throwable> exceptions, final Throwable e) {
        if (e == null) {
            return;
        }

        exceptions.add(e);
        walkExceptions(exceptions, e.getCause());
    }
    
    
    /**
     * Find the first exception that is interesting to us.
     * 
     * Handles ServletException's getRootCause method; without that you may
     * miss the actual exceptions you're looking for.
     * 
     * As long as the provided throwable is not null, this method
     * will not return null.
     */
    public static Throwable getInterestingThrowable(final Throwable e, Class<? extends Throwable>[] uninterestingExceptions) {
        if (isUninteresting(e, uninterestingExceptions)) {
            Throwable nestedE = getCause(e);
            if (nestedE != null) {
                return getInterestingThrowable(nestedE, uninterestingExceptions);
            }
        }
        return e;
    }
    
    @SuppressWarnings("unchecked")
    private static boolean isUninteresting(Throwable e, Class[] uninterestingExceptions) {
    	if (e != null) {
	        for (Class clazz : uninterestingExceptions) {
	            if (clazz.isAssignableFrom(e.getClass())) {
	                return true;
	            }
	        }
    	}
        return false;
    }

    private static Throwable getCause(Throwable e) {
    	Throwable result = e.getCause();
        if (e instanceof ServletException) {
        	ServletException nse = ((ServletException)e);  
            if (nse.getRootCause() != null) {
            	result = nse.getRootCause();
            }
        }
        return result;
    }
}
