package uk.ac.warwick.util.core;

import junit.framework.TestCase;
import uk.ac.warwick.util.core.spring.FileUtils;

public final class FileNameExtensionSplitterTest extends TestCase {
	public void testNoExtension() {
		String fileName = "abcde";
		assertEquals(fileName, FileUtils.getFileNameWithoutExtension(fileName));
		assertEquals("", FileUtils.getLowerCaseExtension(fileName));
	}
	
	public void testMoreThanOneExtension() {
		String fileNameWithBadExtension = "abcde.ext";
		String extension = "abc";
		String fileName = fileNameWithBadExtension + "." + extension;
		
		assertEquals("fileName", fileNameWithBadExtension, FileUtils.getFileNameWithoutExtension(fileName));
		assertEquals("extension", extension, FileUtils.getLowerCaseExtension(fileName));
	}
	
	public void testExtension() {
		String fileNameWithOutExtension = "abcde";
		String extension = "abc";
		String fileName = fileNameWithOutExtension + "." + extension;
		
		assertEquals("fileName", fileNameWithOutExtension, FileUtils.getFileNameWithoutExtension(fileName));
		assertEquals("extension", extension, FileUtils.getLowerCaseExtension(fileName));
	}
	
	public void testWithTrailingSlash() {
		String fileNameWithOutExtension = "abcde";
		String fileName = fileNameWithOutExtension;
		
		assertEquals("fileName", fileName, FileUtils.getFileNameWithoutExtension(fileName + "."));
		assertEquals("extension", "", FileUtils.getLowerCaseExtension(fileName));
	}
	
	public void testCompareExtensions(){
		String filename = "foobar.baz";
		assertTrue("Extension match failed",FileUtils.extensionMatches(filename, "baz"));
		assertTrue("Extension match failed",FileUtils.extensionMatches(filename, ".baz"));
		assertTrue("Extension match failed",FileUtils.extensionMatches(filename, ".BAZ"));
		assertTrue("Extension match failed",FileUtils.extensionMatches(filename, "BaZ"));
		assertFalse("Extension match succeeded unexpectedly!",FileUtils.extensionMatches(filename, "ba"));
		assertFalse("Extension match succeeded unexpectedly!",FileUtils.extensionMatches(filename, "az"));

	}
}
