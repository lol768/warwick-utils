package uk.ac.warwick.util.core;

import java.io.File;
import java.io.IOException;

import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.AbstractFileBasedTest;

public final class FileUtilsCopyTest extends AbstractFileBasedTest {
    public void testCopyFile() throws IOException {
        File fileToCopy = new File(root, "fileToCopy");
        if (!fileToCopy.createNewFile()) {
            fail("cannot create " + fileToCopy);
        }
        FileCopyUtils.copy(StringUtils.create("contents of file"), fileToCopy);
        File target = new File(root, "newFile");
        FileUtils.copy(fileToCopy, target, true);
        assertTrue("file exists", target.exists());
        assertTrue("file is a file", target.isFile());
        String sourceContents = new String(FileCopyUtils.copyToByteArray(fileToCopy));
        String targetContents = new String(FileCopyUtils.copyToByteArray(target));
        assertEquals(sourceContents, targetContents);
    }

    public void testCopyEmptyDir() throws IOException {
        File dirToCopy = new File(root, "dirToCopy");
        if (!dirToCopy.mkdir()) {
            fail("cannot create directory " + dirToCopy);
        }
        File target = new File(root, "newDir");
        FileUtils.copy(dirToCopy, target, true);

        assertTrue("dir exists", target.exists());
        assertTrue("file is a dir", target.isDirectory());
    }

// FIXME: Sanity checks check with a file seperator of /, but in Windows it's \, which makes the tests fail
// and they recurse to infinity :(
//    public void testCopyDirIntoItselfForRecursionWhenTargetDoesntExist() throws IOException {
//        File dirToCopy = super.createDirectory(new File(root, "firstDir"));
//
//        File target = new File(root, "firstDir/secondDir");
//        try {
//            FileUtils.copy(dirToCopy, target, true);
//            fail("Should not support copying source into target when target is a child of source.");
//        } catch (IllegalArgumentException e) {
//            // fine.
//        }
//    }
//
//    public void testCopyDirIntoItselfForRecursionWhenTargetDoesExistButEmptyDirectory() throws IOException {
//        File dirToCopy = super.createDirectory(new File(root, "firstDir"));
//        
//        File target = super.createDirectory(new File(root, "firstDir/secondDir"));
//        
//        try {
//            FileUtils.copy(dirToCopy, target, true);
//            fail("Should not support copying source into target when target is a child of source.");
//        } catch (IllegalArgumentException e) {
//            // fine.
//        }
//    }
//
//    public void testCopyDirIntoItselfForRecursionWhenTargetDoesExistAndAlsoContainsDirectories() throws IOException {
//        File dirToCopy = super.createDirectory(new File(root, "firstDir"));
//        
//        File target = super.createDirectory(new File(root, "firstDir/secondDir"));
//        
//        target = super.createDirectory(new File(root, "firstDir/secondDir/thirdDir"));
//        
//        try {
//            FileUtils.copy(dirToCopy, target, true);
//            fail("Should not support copying source into target when target is a child of source.");
//        } catch (IllegalArgumentException e) {
//            // fine.
//        }
//    }

    public void testCopyDirectoryWithEmptyDirectory() throws IOException {
        File dirToCopy = new File(root, "dirToCopy");
        if (!dirToCopy.mkdir()) {
            fail("cannot create directory " + dirToCopy);
        }
        File nestedDirToCopy = new File(root, "dirToCopy/nestedDir");
        if (!nestedDirToCopy.mkdir()) {
            fail("cannot create directory " + nestedDirToCopy);
        }

        File target = new File(root, "target");
        FileUtils.copy(dirToCopy, target, true);

        assertTrue("dir exists", target.exists());
        assertTrue("file is a dir", target.isDirectory());
        File newNestedDir = new File(target, nestedDirToCopy.getName());
        assertTrue("nested dir is dir", newNestedDir.isDirectory());
        assertTrue("nested dir exists", newNestedDir.exists());
    }

    public void testCopyDirectoryWithEmptyDirectoryButNotRecursive() {
//        fail ("not implemented");
    }

    public void testCopyDirectoryWithDirectoryAndFile() throws IOException {
        File dirToCopy = new File(root, "dirToCopy");
        if (!dirToCopy.mkdir()) {
            fail("cannot create directory " + dirToCopy);
        }
        File nestedDirToCopy = new File(dirToCopy, "nestedDir");
        if (!nestedDirToCopy.mkdir()) {
            fail("cannot create directory " + nestedDirToCopy);
        }
        File nestedFileToCopy = new File(dirToCopy, "nestedFile");
        if (!nestedFileToCopy.createNewFile()) {
            fail("cannot create directory " + nestedFileToCopy);
        }
        FileCopyUtils.copy(StringUtils.create("contents of file"), nestedFileToCopy);

        File target = new File(root, "target");
        FileUtils.copy(dirToCopy, target, true);

        assertTrue("dir exists", target.exists());
        assertTrue("file is a dir", target.isDirectory());
        File newNestedDir = new File(target, nestedDirToCopy.getName());
        assertTrue("nested dir is dir", newNestedDir.isDirectory());
        assertTrue("nested dir exists", newNestedDir.exists());
        File newNestedFile = new File(target, nestedFileToCopy.getName());
        String sourceContents = new String(FileCopyUtils.copyToByteArray(nestedFileToCopy));
        String targetContents = new String(FileCopyUtils.copyToByteArray(newNestedFile));
        assertEquals(sourceContents, targetContents);
    }

    public void testCopyDirectoryWithDirectoryAndFileButNotRecursive() throws IOException {
        File dirToCopy = new File(root, "dirToCopy");
        if (!dirToCopy.mkdir()) {
            fail("cannot create directory " + dirToCopy);
        }
        File nestedDirNotToCopy = new File(dirToCopy, "nestedDir");
        if (!nestedDirNotToCopy.mkdir()) {
            fail("cannot create directory " + nestedDirNotToCopy);
        }
        File nestedFileToCopy = new File(dirToCopy, "nestedFile");
        if (!nestedFileToCopy.createNewFile()) {
            fail("cannot create directory " + nestedFileToCopy);
        }
        FileCopyUtils.copy(StringUtils.create("contents of file"), nestedFileToCopy);

        File target = new File(root, "target");
        FileUtils.copy(dirToCopy, target, false);

        assertTrue("dir exists", target.exists());
        assertTrue("file is a dir", target.isDirectory());
        File newNestedDir = new File(target, nestedDirNotToCopy.getName());
        assertFalse("nested dir should not exist", newNestedDir.exists());
        File newNestedFile = new File(target, nestedFileToCopy.getName());
        String sourceContents = new String(FileCopyUtils.copyToByteArray(nestedFileToCopy));
        String targetContents = new String(FileCopyUtils.copyToByteArray(newNestedFile));
        assertEquals(sourceContents, targetContents);
    }
}
