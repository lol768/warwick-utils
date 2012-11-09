package uk.ac.warwick.util.core;


/**
 * A ThreadSafeStopWatch encapsulates a StopWatch class wrapped around a
 * ThreadLocal. This class fails silently if undefined behaviour is invoked,
 * i.e. if nested start/stop calls are attempted, the nested call will be
 * completely ignored.
 * 
 * @author Mat Mannion
 */
public final class ThreadSafeStopWatch {

    private static ThreadLocal<StopWatch> stopWatch = createNewStopWatch();

    private ThreadSafeStopWatch() {
    }

    public static void start(final String taskName) {
        stopWatch.get().start(taskName);
    }

    /**
     * Stop the current task, returning the time it took in millis
     */
    public static long stop() {
        return stopWatch.get().stop();
    }

    public static String prettyPrint() {
        return stopWatch.get().prettyPrint();
    }

    public static void destroy() {
        stopWatch.remove();
    }

    private static ThreadLocal<StopWatch> createNewStopWatch() {
        return new ThreadLocal<StopWatch>() {
            protected synchronized StopWatch initialValue() {
                return new StopWatch();
            }
        };
    }

}
