package uk.ac.warwick.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import uk.ac.warwick.util.core.spring.FileUtils;

public abstract class AbstractJUnit4FileBasedTest {
    protected static File root;

    @BeforeClass
    public static void setUp() throws Exception {
        // create the temporary directory
        File tmpRoot = File.createTempFile("fileBasedTests", null);
        String tmpName = tmpRoot.getName();
        File rootParent = tmpRoot.getParentFile();
        tmpRoot.delete();
        root = new File(rootParent, tmpName);
        if (!root.mkdir()) {
            fail("Cannot create " + root);
        }
    }
    
    @After
    public void clean() throws Exception {
        for (File file : root.listFiles()) {
            try {
                FileUtils.recursiveDelete(file);
            } catch (IllegalStateException e) {
                // never mind
            }
        }
    }

    public static File getRoot() {
        return root;
    }

    @AfterClass
    public static final void tearDown() throws Exception {
        try {
            FileUtils.recursiveDelete(root);
        } catch (IllegalStateException e) {
            // Didn't do very well at deleting, never mind.
        }
    }

    protected File createDirectory(final File directory) {
        if (!directory.mkdir()) {
            fail("cannot create directory " + directory);
        }
        return directory;
    }
}