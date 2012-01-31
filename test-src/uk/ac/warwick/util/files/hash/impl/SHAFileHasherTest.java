package uk.ac.warwick.util.files.hash.impl;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

public final class SHAFileHasherTest {
    
    private final SHAFileHasher hasher = new SHAFileHasher();

    @Test
    public void hash() throws Exception {
        // If this hash changes between environments we have a serious problem.
        String expectedHash = "f5098abdf29d38f92350fb9bcf06c6f0a2ec8f52";
        
        InputStream is = getClass().getResourceAsStream("/hugeimage.bmp");
        assertNotNull(is);
        
        String hash = hasher.hash(is); 
        
        assertEquals(expectedHash, hash);
    }

}
