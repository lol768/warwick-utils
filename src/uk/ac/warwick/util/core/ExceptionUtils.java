package uk.ac.warwick.util.core;

import java.util.ArrayList;
import java.util.List;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static Throwable retrieveException(final Exception e, final Class clazz) {
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
}
