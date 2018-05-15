package uk.ac.warwick.util.files.imageresize;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.files.DefaultFileStoreStatistics;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.imageresize.ImageResizer.FileType;
import uk.ac.warwick.util.files.impl.FileBackedHashFileReference;

import javax.media.jai.PlanarImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class JAIImageResizerTest {
    ZonedDateTime lastModified = ZonedDateTime.now();

    private final Mockery m = new JUnit4Mockery();

    private final HashFileStore fileStore = m.mock(HashFileStore.class);

    @Before
    public void setup() throws Exception {
        m.checking(new Expectations() {{
            allowing(fileStore).getStatistics(); will(returnValue(new DefaultFileStoreStatistics(fileStore)));
        }});
    }

    /**
     * SBTWO-3903 a big zTXt chunk in a PNG highlighted a crappy decoder implementation in JAI -
     * it would complete but extremely slowly due to single-byte reading and String appends.
     * Patched JAI codec is nice and fast.
     */
    @Test(timeout=5000)
    public void decodingZtxtChunks() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/bad-zTXt-chunk.png"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ByteSource.wrap(input), null, null, output, 300, 300, FileType.jpg);
    }

    @Test
    public void resizeTallThinImage() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 50;
        int maxHeight = 165;
        resizer.renderResized(ByteSource.wrap(input), null, null, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = ImageReadUtils.read(output);
        assertEquals(maxWidth, result.getWidth());
        assertTrue(maxHeight > result.getHeight());
    }

    @Test
    public void resizeAsFileNotByteArray() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();
        // tallThinSample.jpg is 100 x 165 px
        File input = new File(this.getClass().getResource("/tallThinSample.jpg").getFile());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 50;
        int maxHeight = 165;
        resizer.renderResized(Files.asByteSource(input), null, null, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = ImageReadUtils.read(output);
        assertEquals(maxWidth, result.getWidth());
        assertTrue(maxHeight > result.getHeight());
    }

    /**
     * [SBTWO-1920] We didn't have a test that actually tested maxHeight worked;
     * and it didn't. Test that it does.
     */
    @Test
    public void resizeTallThinImageByHeight() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 100;
        int maxHeight = 155;
        resizer.renderResized(ByteSource.wrap(input), null, null, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = ImageReadUtils.read(output);

        // subsample average avoids black line at the bottom
        assertEquals(154, result.getHeight());
    }

    @Test
    public void resizeShortWideImage() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // shortWide.jpg is 200 x 150px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/shortWideSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ByteSource.wrap(input), null, null, output, 50, 165, FileType.jpg);

        PlanarImage result = ImageReadUtils.read(output);
        assertEquals(50, result.getWidth());
    }

    /**
     * SBTWO-3196 - we were only testing with byte arrays, meanwhile reading from a file started failing.
     */
    @Test
    public void fileReferenceInput() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();
        File f = new File(this.getClass().getResource("/tallThinSample.jpg").getFile());
        FileReference ref = new FileBackedHashFileReference(fileStore, f, new HashString("abcdef"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, 50, 165, FileType.jpg);

        assertEquals(output.size(), resizer.getResizedImageLength(ref.asByteSource(), ref.getHash(), lastModified, 50, 165, FileType.jpg));
    }

    @Test
    public void dontResizeLargerThanOriginal() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ByteSource.wrap(input), null, null, output, 150, 200, FileType.jpg);

        PlanarImage result = ImageReadUtils.read(output);
        assertEquals(100, result.getWidth());
        assertEquals(165, result.getHeight());

        assertEquals("Output should be the same as input", input.length,output.toByteArray().length);
    }

    @Test
    public void PNGResizing() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // award.png is 220x233px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/award.png"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ByteSource.wrap(input), null, null, output, 110, 116, FileType.png);

        PlanarImage result = ImageReadUtils.read(output);
        FileCopyUtils.copy(output.toByteArray(), new File("/tmp/file.png"));

        // subsample average avoids black line at the bottom
        assertEquals(109, result.getWidth());
        assertEquals(116, result.getHeight());
    }

    @Test
    public void resizeTheWorld() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();

        File input = new File(this.getClass().getResource("/pendulum_crop1.jpg").getFile());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 350;
        int maxHeight = 149;
        resizer.renderResized(Files.asByteSource(input), null, null, output, maxWidth, maxHeight, FileType.jpg);

        // Image resizer will refuse to resize.
        Pair<Integer, Integer> dimensions = JAIImageResizer.getDimensions(new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(24803, dimensions.getLeft().intValue());
        assertEquals(10559, dimensions.getRight().intValue());
    }

    /**
     * Maybe there used to be something wrong with this image in the past but it seems to resize fine now.
     * ImageIO may work around whatever is "bad" about it.
     */
    @Test
    public void badImage() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();

        File f = new File(this.getClass().getResource("/October.jpg").getFile());
        FileReference ref = new FileBackedHashFileReference(fileStore, f, new HashString("abcdef"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, 50, 165, FileType.jpg);

        assertEquals(output.size(), resizer.getResizedImageLength(ref.asByteSource(), ref.getHash(), lastModified, 50, 165, FileType.jpg));

        Pair<Integer, Integer> dimensions = JAIImageResizer.getDimensions(new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(50, dimensions.getLeft().intValue());
        assertEquals(25, dimensions.getRight().intValue());
    }

}
