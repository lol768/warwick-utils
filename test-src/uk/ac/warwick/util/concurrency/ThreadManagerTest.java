package uk.ac.warwick.util.concurrency;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.junit.Test;

import uk.ac.warwick.util.concurrency.ThreadManager.ThreadSorting;


public class ThreadManagerTest {

    @Test(timeout=2000) public void list() throws Exception {
        Thread t = startForeverLooping("http-processor-23");
        
        ThreadManager ozymandias = new ThreadManager();
        
        ozymandias.listAllThreads(ThreadSorting.none);
        ozymandias.listAllThreads(ThreadSorting.id);
        ozymandias.listAllThreads(ThreadSorting.name);
        
        long lastId = -1;
        for (Entry<Thread, StackTraceElement[]> entry : ozymandias.getAllThreads(ThreadSorting.id)) {
            long id = entry.getKey().getId();
            assertTrue("Not in ID order", id > lastId);
            lastId = id;
        }
        
        ozymandias.kill(t.getId(), "http-processor-23");
        
        t.join();
    }
    
    private Thread startForeverLooping(String name) {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    String fun = "Wheeeeeee!!";
                }
            }
        };
        t.setDaemon(true);
        t.setName(name);
        t.start();
        return t;
    }
    
}
