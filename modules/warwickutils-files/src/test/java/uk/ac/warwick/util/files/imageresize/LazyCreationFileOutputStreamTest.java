package uk.ac.warwick.util.files.imageresize;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.util.Random;

import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.files.imageresize.LazyCreationFileOutputStream;


public class LazyCreationFileOutputStreamTest {
    @Test public void createFileOnWrite() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"));
        assertTrue( root.exists() );
        File newFile = new File(root, "create" + new Random().nextInt(100));
        newFile.deleteOnExit();

        LazyCreationFileOutputStream output = new LazyCreationFileOutputStream(newFile);
        assertFalse( newFile.exists() );
        output.write("DATA".getBytes());
        output.write("DATA!".getBytes());
        assertTrue( newFile.exists() );
        
        output.close();
        assertEquals("DATADATA!", FileCopyUtils.copyToString(new FileReader(newFile)));
    }
}
