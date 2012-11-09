package uk.ac.warwick.util.core.spring;

import junit.framework.TestCase;

public class FileUtilsConvertToSafeFileNameTest extends TestCase {
    public void testBadCharacters() {
        String[] goodNames = {"a.gfd", "abc_-012a.abf"};
        String[] badNames = {"(", ")", "{", "}", ">", "<", "!", "\"", "$", "%", "^", "&",
                "*", "+", "=", "[", "]", ":", ";", "@", "'", "~", "#", "?", "/", "|"};

        for (String good: goodNames) {
            assertEquals("good: " + good, good, FileUtils.convertToSafeFileName(good));
        }

        for (String bad: badNames) {
            assertEquals("bad: " + bad, "", FileUtils.convertToSafeFileName(bad));
        }

        for (String good: goodNames) {
            for (String bad: badNames) {
                String s = bad + good + bad;
                assertEquals("goodAndBad: " + s, good, FileUtils.convertToSafeFileName(s));
            }
        }
    }

    /**
     * Multiple dots are allowed, but not at the start.
     */
    public void testMoreThanOneExtension() {
        String fileName = "ab.c.def";
        String badFileName = ".ab.c.def";
        assertEquals(fileName, FileUtils.convertToSafeFileName(badFileName));
    }

    public void testBadExtension() {
        String fileName = "abc.";
        String badFileName = fileName + ")))";
        assertEquals(fileName, FileUtils.convertToSafeFileName(badFileName));
    }

    public void testSpacesAreConverted() {
        String badFileName = "a space.tx t";
        String goodFileName = "a_space.tx_t";
        assertEquals(goodFileName, FileUtils.convertToSafeFileName(badFileName));
    }
    
    public void testLowerCaseConverting() {
        String badFileName = "ABCde.GiF";
        String goodFileName = "abcde.gif";
        assertEquals(goodFileName, FileUtils.convertToSafeFileName(badFileName));
    }

    public void testPathIsExtracted() {
        String fileName = "abc.def";
        String badFileName = "/gjhsd/sd/abc.def";
        assertEquals(fileName, FileUtils.convertToSafeFileName(badFileName));
    }
    
    public void testLowerAndSpecial() {
        String fileName = "hello_world";
        String badFileName = "Hello?!?!_%world";
        assertEquals(fileName, FileUtils.convertToSafeFileName(badFileName));
    }
}
