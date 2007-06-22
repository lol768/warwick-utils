package uk.ac.warwick.util.core;

import org.jmock.MockObjectTestCase;

public final class GetFileNameFileUtilsTest extends MockObjectTestCase {
    public void testWithRoot() {
        String url = "/";
        assertEquals("path should be empty", "", FileUtils.getFileName(url));
    }

    public void testWithPath() {
        String path = "path";
        String url = "/services/its/" + path;
        assertEquals(path, FileUtils.getFileName(url));
    }
}
