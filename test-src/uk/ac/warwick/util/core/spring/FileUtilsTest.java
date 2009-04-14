package uk.ac.warwick.util.core.spring;

import junit.framework.TestCase;

public class FileUtilsTest extends TestCase {

	public void testGetLowerCaseExtension() {
		assertEquals("flv", FileUtils.getLowerCaseExtension("http://example.com/somewhere/video.FLV"));
	}
	
	public void testGetFilenameWithoutExtension() {
		assertEquals("fab", FileUtils.getFileNameWithoutExtension("fab.txt"));
		assertEquals("fab.tar", FileUtils.getFileNameWithoutExtension("fab.tar.gz"));
		assertEquals(".hidden", FileUtils.getFileNameWithoutExtension(".hidden"));
		assertEquals(".hidden", FileUtils.getFileNameWithoutExtension(".hidden.swp"));
	}

}
