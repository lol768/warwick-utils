package uk.ac.warwick.util.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class StopWatchTest {

    @Test
    public void getCurrentTask() {
        StopWatch sw = new StopWatch();
        sw.start("task1");
        sw.start("task2");
        
        assertTrue(sw.hasRunningTask());
        assertEquals("task1", sw.getCurrentTask().getTaskName());
        assertTrue(sw.getCurrentTask().hasRunningSubTask());
        assertEquals("task2", sw.getCurrentTask().getCurrentSubTask().getTaskName());
        
        assertNotNull(sw.getCurrentTask().getStartTime());
        assertNotNull(sw.getCurrentTask().getCurrentSubTask().getStartTime());
    }

    @Test
    public void prettyPrint() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start("task1");        
        sw.start("task2");
        sw.stop();
        sw.stop();
        
        // fix the results
        sw.setTotalTimeMillis(1500);
        sw.getTaskInfo()[0].setTimeMillis(1500);
        sw.getTaskInfo()[0].getSubTasks().get(0).setTimeMillis(500);
        
        assertEquals("StopWatch: running time (millis) = 1500\n"
                   + "---------------------------------------------------\n"
                   + "ms       %       Task name\n"
                   + "---------------------------------------------------\n"
                   + "1500 ms   100%    task1\n"
                   + "->500 ms   33%    task2\n", sw.prettyPrint());
    }

}
