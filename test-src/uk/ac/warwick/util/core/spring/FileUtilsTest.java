package uk.ac.warwick.util.core.spring;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.AbstractFileBasedTest;
import uk.ac.warwick.util.core.StringUtils;

public class FileUtilsTest extends AbstractFileBasedTest {
    public void testTemporaryFile() throws FileNotFoundException, IOException {

        int numberOfThreads = 10;
        final List<File> files = new ArrayList<File>(numberOfThreads);
        final List<String> contentsArray = new ArrayList<String>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            Runnable r = new Runnable() {
                public void run() {
                    String contents = " the contents " + contentsArray.size();
                    contentsArray.add(contents);
                    InputStream is = new ByteArrayInputStream(StringUtils.create(contents));
                    File tmpFile = FileUtils.createFile(contents, is, new File(root, "/tmp/sb-flows"));
                    files.add(tmpFile);
                }
            };
            r.run();
        }
        assertEquals("sanity test files", numberOfThreads, files.size());
        assertEquals("sanity test contents", numberOfThreads, contentsArray.size());

        for (int i = 0; i < numberOfThreads; i++) {
            File file = files.get(i);
            assertTrue("file " + file + " exists", file.exists());
            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            String contents = new String(bytes);

            assertEquals("contents", contentsArray.get(i), contents);
        }
    }

    /**
     * See also FileUtilsConvertToSafeFileNameTest
     */
    public void testConvertToSafeFileName() throws Exception {
        verifyFileNameConversion("validfile.xml");
        verifyFileNameConversion("properly_formatted_name.txt");

        verifyFileNameConversion("Spaced file name.jpg", "spaced_file_name.jpg");
        verifyFileNameConversion("CAPITAL.GIF", "capital.gif");
        verifyFileNameConversion("excited!.txt", "excited.txt");
        verifyFileNameConversion("[b]utilityBar fixes.txt[]", "butilitybar_fixes.txt");

        verifyFileNameConversion("'Very elaborate,' file name; maybe.gif", "very_elaborate_file_name_maybe.gif");

        verifyFileNameConversion("archive.tar.gz");
        verifyFileNameConversion("application-1.0.6-build.zip");

        // remove any starting dot.
        verifyFileNameConversion(".dotstart.zip", "dotstart.zip");
        // hey not so fast
        verifyFileNameConversion(".....dotstart.zip", "dotstart.zip");
    }

    private void verifyFileNameConversion(final String input, final String expected) {
        assertEquals(expected, uk.ac.warwick.util.core.spring.FileUtils.convertToSafeFileName(input));
    }

    // Shortcut for when the input is expected to return unchanged.
    private void verifyFileNameConversion(final String input) {
        verifyFileNameConversion(input, input);
    }

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
