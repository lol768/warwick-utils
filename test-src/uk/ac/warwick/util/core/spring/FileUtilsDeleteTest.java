package uk.ac.warwick.util.core.spring;

import java.io.File;
import java.io.IOException;

import uk.ac.warwick.util.AbstractFileBasedTest;

public final class FileUtilsDeleteTest extends AbstractFileBasedTest {
	public void testDeleteFile() throws IOException { 
		File fileToDelete = new File(root.getParentFile(), "fileToDelete");
		if (!fileToDelete.createNewFile()) {
			fail("cannot create " + fileToDelete);
		}

		FileUtils.recursiveDelete(fileToDelete);
		assertFalse("file exists", fileToDelete.exists());
	}
	
	public void testDeleteEmptyDir() throws IOException { 
		File dirToDelete = new File(root.getParentFile(), "dirToDelete");
		if (!dirToDelete.mkdir()) {
			fail("cannot create directory " + dirToDelete);
		}

		FileUtils.recursiveDelete(dirToDelete);
		assertFalse("dir exists", dirToDelete.exists());
	}
	
	public void testDeleteDirectoryWithEmptyDirectory() throws IOException { 
		File dirToDelete = new File(root.getParentFile(), "dirToDelete");
		if (!dirToDelete.mkdir()) {
			fail("cannot create directory " + dirToDelete);
		}
		File nestedDirToDelete = new File(dirToDelete, "nestedDir");
		if (!nestedDirToDelete.mkdir()) {
			fail("cannot create directory " + nestedDirToDelete);
		}
		

		FileUtils.recursiveDelete(dirToDelete);
		assertFalse("dir exists", dirToDelete.exists());
		assertFalse("nested dir exists", nestedDirToDelete.exists());		
	}
	
	public void testDeleteDirectoryWithDirectoryAndFile() throws IOException {
		File dirToDelete = new File(root.getParentFile(), "dirToDelete");
		if (!dirToDelete.mkdir()) {
			fail("cannot create directory " + dirToDelete);
		}
		File nestedDirToDelete = new File(dirToDelete, "nestedDir");
		if (!nestedDirToDelete.mkdir()) {
			fail("cannot create directory " + nestedDirToDelete);
		}
		File nestedFileToDelete = new File(dirToDelete, "nestedFile");
		if (!nestedFileToDelete.createNewFile()) {
			fail("cannot create directory " + nestedFileToDelete);
		}
		

		FileUtils.recursiveDelete(dirToDelete);
		assertFalse("dir exists", dirToDelete.exists());
		assertFalse("nested dir exists", nestedDirToDelete.exists());
		assertFalse("nested file exists", nestedFileToDelete.exists());		
	}
}
