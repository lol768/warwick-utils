package uk.ac.warwick.util;

import java.io.File;

import org.jmock.MockObjectTestCase;

import uk.ac.warwick.util.core.spring.FileUtils;

public abstract class AbstractFileBasedTest extends MockObjectTestCase {
    protected File root;

    public final void setUp() throws Exception {
        // create the temporary directory
        File tmpRoot = File.createTempFile("fileBasedTests", null);
        String tmpName = tmpRoot.getName();
        File rootParent = tmpRoot.getParentFile();
        tmpRoot.delete();
        root = new File(rootParent, tmpName);
        if (!root.mkdir()) {
            fail("Cannot create " + root);
        }
        onSetUp();
    }
    protected void onSetUp() throws Exception {

    }

    public File getRoot() {
        return root;
    }

    public final void tearDown() throws Exception {
        try {
            FileUtils.recursiveDelete(root);
        } catch (IllegalStateException e) {
            //Didn't do very well at deleting, never mind.
        }
        onTearDown();
    }

    protected void onTearDown() throws Exception {
    }
    
    protected File createDirectory(final File directory) {
        if (!directory.mkdir()) {
            fail("cannot create directory " + directory);
        }
        return directory;
    }
}