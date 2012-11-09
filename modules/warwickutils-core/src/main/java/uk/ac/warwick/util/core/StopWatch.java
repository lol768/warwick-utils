
package uk.ac.warwick.util.core;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import uk.ac.warwick.util.collections.Pair;

public final class StopWatch {

    public static final Comparator<Task> TIME_COMPARATOR = new Comparator<Task>() {
        public int compare(final Task arg0, final Task arg1) {
            return ((Long) (arg1.timeMillis)).compareTo((Long) (arg0.timeMillis));
        }
    };

    private static final double MAX_PERCENTAGE = 100.0;

    private static final double MILLISECONDS_IN_SECOND = 1000.0;
    
    private static final int DEFAULT_DISPLAY_MS_THRESHOLD = 10;

    /** List of TaskInfo objects */
    private final List<Task> taskList = new LinkedList<Task>();

    private int taskCount;

    /** Total running time */
    private long totalTimeMillis;
    
    private int displayThresholdInMs = DEFAULT_DISPLAY_MS_THRESHOLD;


    /**
     * Start a named task. The results are undefined if <code>stop</code> or
     * timing methods are called without invoking this method.
     * 
     * @param taskName
     *            the name of the task to start
     * @see #stop
     */
    public void start(final String taskName) throws IllegalStateException {
        if (hasRunningTask()) {
            getCurrentTask().addAndStartSubTask(taskName);
        } else {
            Task task = new Task(taskName, this);
            task.start();
            taskList.add(task);
        }
    }

    /**
     * Stop the current task. The results are undefined if timing methods are
     * called without invoking at least one pair <code>start</code>/<code>stop</code>
     * methods.
     * 
     * @see #start
     */
    public long stop() throws IllegalStateException {
        //ignore fails
        if (hasRunningTask()) {
            long lastTime = getCurrentTask().stop().getRight();

            if (!getCurrentTask().isRunning()) {
                this.totalTimeMillis += lastTime;
                ++this.taskCount;
            }
            
            return lastTime;
        }
        
        return -1; // Some kind of failure condition has caused us not to return
    }

    public Task getCurrentTask() {
        if (taskList.isEmpty()) {
            return null;
        } else {
            return taskList.get(taskList.size() - 1);
        }
    }

    public boolean hasRunningTask() {
        Task task = getCurrentTask();
        if (task == null) {
            return false;
        } else {
            return task.isRunning();
        }
    }
    
    /** Used for testing */
    void setTotalTimeMillis(long totalTime) {
        this.totalTimeMillis = totalTime;
    }

    public long getTotalTimeMillis() {
        return totalTimeMillis;
    }

    public double getTotalTimeSeconds() {
        return totalTimeMillis / MILLISECONDS_IN_SECOND;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public Task[] getTaskInfo() {
        return (Task[]) this.taskList.toArray(new Task[this.taskList.size()]);
    }

    public String shortSummary() {
        return "StopWatch: running time (millis) = " + getTotalTimeMillis();
    }

    public String prettyPrint() {
        StringBuffer sb = new StringBuffer(shortSummary());
        sb.append('\n');
        sb.append("---------------------------------------------------\n");
        sb.append("ms       %       Task name\n");
        sb.append("---------------------------------------------------\n");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setGroupingUsed(false);

        Collections.sort(taskList, TIME_COMPARATOR);

        for (Task task: taskList) {
            task.prettyPrint(sb, getTotalTimeSeconds(), nf, pf, 0);
        }

        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(shortSummary());

        Task[] tasks = getTaskInfo();
        for (int i = 0; i < tasks.length; i++) {
            sb.append("; [" + tasks[i].getTaskName() + "] took " + tasks[i].getTimeMillis());
            long percent = Math.round((MAX_PERCENTAGE * tasks[i].getTimeSeconds()) / getTotalTimeSeconds());
            sb.append(" = " + percent + "%");
        }

        return sb.toString();
    }
    
    public void setDisplayThresholdInMs(int displayThresholdInMs) {
		this.displayThresholdInMs = displayThresholdInMs;
	}

	private int getDisplayMsThreshold() {
        return displayThresholdInMs;
    }

    /**
     * Inner class to hold data about one task executed within the stop watch.
     */
    public static final class Task {

        private static final double MILLISECONDS_IN_SECOND = 1000.0;

        private final String taskName;
        
        private final StopWatch sw;

        private long startTime;

        private long endTime;

        private long timeMillis;

        private boolean running;

        private List<Task> subTasks = new LinkedList<Task>();

        private Task(final String theTaskName, final StopWatch stopWatch) {
            this.taskName = theTaskName;
            this.sw = stopWatch;
        }

        public void start() {
            startTime = System.currentTimeMillis();
            running = true;
        }

        public void addAndStartSubTask(final String theTaskName) {
            if (hasRunningSubTask()) {
                getCurrentSubTask().addAndStartSubTask(theTaskName);
            } else {
                Task task = new Task(theTaskName, sw);
                subTasks.add(task);
                task.start();
            }
        }

        public Pair<Boolean, Long> stop() {
            if (hasRunningSubTask()) {
                return getCurrentSubTask().stop();
            } else {
                endTime = System.currentTimeMillis();
                running = false;
                timeMillis = endTime - startTime;

                return Pair.of(true, timeMillis);
            }
        }

        public boolean isRunning() {
            return running;
        }

        /**
         * Return the name of this task.
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * Return the time in milliseconds this task took.
         */
        public long getTimeMillis() {
            return timeMillis;
        }

        public double getTimeSeconds() {
            return timeMillis / MILLISECONDS_IN_SECOND;
        }

        public Task getCurrentSubTask() {
            if (subTasks.isEmpty()) {
                return null;
            } else {
                return subTasks.get(subTasks.size() - 1);
            }
        }

        public boolean hasRunningSubTask() {
            Task subTask = getCurrentSubTask();
            if (subTask == null) {
                return false;
            } else {
                return subTask.isRunning();
            }
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(final long endTime) {
            this.endTime = endTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(final long startTime) {
            this.startTime = startTime;
        }

        public List<Task> getSubTasks() {
            return subTasks;
        }

        public void setSubTasks(final List<Task> subTasks) {
            this.subTasks = subTasks;
        }

        public void setRunning(final boolean running) {
            this.running = running;
        }

        public void setTimeMillis(final long timeMillis) {
            this.timeMillis = timeMillis;
        }

        public void prettyPrint(final StringBuffer sb, final double totalTimeSeconds, final NumberFormat nf, final NumberFormat pf,
                final int tabs) {
            if (getTimeMillis() > sw.getDisplayMsThreshold()) {
                for (int i = 0; i < tabs; i++) {
                    sb.append("-");
                }

                if (tabs > 0) {
                    sb.append(">");
                }

                sb.append(nf.format(getTimeMillis()) + " ms   ");
                sb.append(pf.format(getTimeSeconds() / totalTimeSeconds) + "    ");
                sb.append(getTaskName());
                sb.append("\n");

                Collections.sort(subTasks, TIME_COMPARATOR);

                for (Task subTask: subTasks) {
                    subTask.prettyPrint(sb, totalTimeSeconds, nf, pf, tabs + 1);
                }
            }
        }

    }

}