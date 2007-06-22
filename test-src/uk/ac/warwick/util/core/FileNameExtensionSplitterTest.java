package uk.ac.warwick.util.core;

import junit.framework.TestCase;

public final class FileNameExtensionSplitterTest extends TestCase {
	public void testNoExtension() {
		String fileName = "abcde";
		assertEquals(fileName, FileUtils.getFileNameWithoutExtension(fileName));
		assertEquals("", FileUtils.getExtension(fileName));
	}
	
	public void testMoreThanOneExtension() {
		String fileNameWithBadExtension = "abcde.ext";
		String extension = "abc";
		String fileName = fileNameWithBadExtension + "." + extension;
		
		assertEquals("fileName", fileNameWithBadExtension, FileUtils.getFileNameWithoutExtension(fileName));
		assertEquals("extension", extension, FileUtils.getExtension(fileName));
	}
	
	public void testExtension() {
		String fileNameWithOutExtension = "abcde";
		String extension = "abc";
		String fileName = fileNameWithOutExtension + "." + extension;
		
		assertEquals("fileName", fileNameWithOutExtension, FileUtils.getFileNameWithoutExtension(fileName));
		assertEquals("extension", extension, FileUtils.getExtension(fileName));
	}
	
	public void testWithTrailingSlash() {
		String fileNameWithOutExtension = "abcde";
		String fileName = fileNameWithOutExtension;
		
		assertEquals("fileName", fileName, FileUtils.getFileNameWithoutExtension(fileName + "."));
		assertEquals("extension", "", FileUtils.getExtension(fileName));
	}
}
