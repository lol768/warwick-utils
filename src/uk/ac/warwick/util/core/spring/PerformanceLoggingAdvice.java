package uk.ac.warwick.util.core.spring;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

import uk.ac.warwick.util.core.ThreadSafeStopWatch;

public final class PerformanceLoggingAdvice implements MethodBeforeAdvice, AfterReturningAdvice, ThrowsAdvice, Serializable {

    private static final long serialVersionUID = -1727631392636075100L;

    public void before(final Method method, final Object[] args, final Object target) throws Throwable {
        String name = target.getClass().getName().substring(target.getClass().getName().lastIndexOf(".") + 1) + "."
                + method.getName();
        name += "(";
        boolean first = true;
        if (args != null) {
            for (Object arg: args) {
                if (first) {
                    first = false;
                } else {
                    name += ", ";
                }
                if (arg == null || arg.toString() == null) {
                    name += "null";
                } else {
                    name += arg.toString();
                }
            }
        }
        name += ")";
        ThreadSafeStopWatch.start(name);
    }

    public void afterReturning(final Object returnValue, final Method method, final Object[] args, final Object target)
            throws Throwable {
        ThreadSafeStopWatch.stop();
    }
    
    /**
     * afterReturning only applies if a method returned successfully; we also need to handle
     * when an exception is thrown, and stop the stopwatch.
     */
    public void afterThrowing(final Exception exception) {
        ThreadSafeStopWatch.stop();
    }

    /*
     * Serializability... horrible :( this stops it doing the default
     * serialization/deserialization by not calling the
     * defaultReadObject/defaultWriteObject stuff... Java's not gonna like this :)
     */

    private void readObject(final ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
    }

    private void writeObject(final ObjectOutputStream aOutputStream) throws IOException {
    }

}
