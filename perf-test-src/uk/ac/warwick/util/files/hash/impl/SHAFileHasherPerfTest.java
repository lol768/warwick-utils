package uk.ac.warwick.util.files.hash.impl;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

import uk.ac.warwick.util.core.StopWatch;

public class SHAFileHasherPerfTest {
    
    private static final int RUNS = 100;
    private final SHAFileHasher hasher = new SHAFileHasher();
    
    StopWatch stopWatch;

    @Test
    public void hash() throws Exception {
        stopWatch = new StopWatch();
        stopWatch.start("timing " + RUNS + " runs");
        
        for (int i=0; i<RUNS; i++) {
            InputStream is = getClass().getResourceAsStream("/iggy-u-warwick-2-closing-ceremony-course-presentations.mp4"); // about 300mb
            assertNotNull(is);
            
            System.out.println(hasher.hash(is));
        }
        
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

}
