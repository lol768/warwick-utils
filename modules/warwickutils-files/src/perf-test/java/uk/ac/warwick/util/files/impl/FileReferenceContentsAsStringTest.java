package uk.ac.warwick.util.files.impl;

import java.io.File;

import org.junit.Test;

import uk.ac.warwick.util.core.StopWatch;

public final class FileReferenceContentsAsStringTest {
    
    private static final int RUNS = 10000;
    
    StopWatch stopWatch;

    @Test
    public void hash() throws Exception {
        stopWatch = new StopWatch();
        stopWatch.start("timing " + RUNS + " runs");
        
        File file = new File("/www/sb2/hashed-html/44/02/66/c9/42/c39ecc76b0c5e619d7a39eaf4f3cc7.data");
        
        HashBackedFileReference ref = new HashBackedFileReference(null, file, null);
        
        for (int i=0; i<RUNS; i++) {
            ref.getContentsAsString();
        }
        
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

}
