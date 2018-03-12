package uk.ac.warwick.util.files.imageresize;

import com.google.common.io.ByteSource;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.imageresize.ImageResizer.FileType;
import uk.ac.warwick.util.files.impl.AbstractFileReference;
import uk.ac.warwick.util.files.impl.FileBackedHashFileReference;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;
import static uk.ac.warwick.util.files.imageresize.ImageReadUtils.read;

public class SingleThreadedImageResizerTest {

    private final SingleThreadedImageResizer resizer = new SingleThreadedImageResizer(new JAIImageResizer());

    @Test
    public void resizeTallThinImage() throws IOException {
        final ZonedDateTime lastModified = ZonedDateTime.now();

        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 50;
        int maxHeight = 165;

        FileReference ref = ref(input);
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = read(output);
        assertEquals(maxWidth, result.getWidth());
        assertTrue(maxHeight > result.getHeight());
    }

    /**
     * [SBTWO-1920] We didn't have a test that actually tested maxHeight worked;
     * and it didn't. Test that it does.
     */
    @Test
    public void resizeTallThinImageByHeight() throws IOException {
        final ZonedDateTime lastModified = ZonedDateTime.now();

        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 0;
        int maxHeight = 155;
        FileReference ref = ref(input);
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = read(output);

        // subsample average avoids black line at the bottom
        assertEquals(154, result.getHeight());
    }

    @Test
    public void resizeShortWideImage() throws IOException {
        final ZonedDateTime lastModified = ZonedDateTime.now();

        // shortWide.jpg is 200 x 150px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/shortWideSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileReference ref = ref(input);
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, 50, 165, FileType.jpg);

        PlanarImage result = read(output);
        assertEquals(50, result.getWidth());
    }

    /**
     * SBTWO-3196 - we were only testing with byte arrays, meanwhile reading from a file started failing.
     */
    @Test
    public void fileReferenceInput() throws Exception {
        final ZonedDateTime lastModified = ZonedDateTime.now();

        File f = new File(this.getClass().getResource("/tallThinSample.jpg").getFile());
        FileReference ref = new FileBackedHashFileReference(null, f, new HashString("abcdef"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, 50, 165, FileType.jpg);

        assertEquals(output.size(), resizer.getResizedImageLength(ref.asByteSource(), ref.getHash(), lastModified, 50, 165, FileType.jpg));
    }

    @Test
    public void dontResizeLargerThanOriginal() throws IOException {
        final ZonedDateTime lastModified = ZonedDateTime.now();

        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileReference ref = ref(input);
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, 150, 200, FileType.jpg);

        PlanarImage result = read(output);
        assertEquals(100, result.getWidth());
        assertEquals(165, result.getHeight());

        assertEquals("Output should be the same as input", input.length,output.toByteArray().length);
    }

    @Test
    public void PNGResizing() throws IOException {
        final ZonedDateTime lastModified = ZonedDateTime.now();

        // award.png is 220x233px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/award.png"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileReference ref = ref(input);
        resizer.renderResized(ref.asByteSource(), ref.getHash(), lastModified, output, 110, 116, FileType.png);

        PlanarImage result = read(output);

        // subsample average avoids black line at the bottom
        assertEquals(109, result.getWidth());
        assertEquals(116, result.getHeight());
    }

    private FileReference ref(final byte[] input) {
        return new AbstractFileReference() {

            public FileData getData() {
                return new FileData() {
                    public ByteSource asByteSource() {
                        return ByteSource.wrap(input);
                    }

                    public long length() {
                        return input.length;
                    }

                    public FileData overwrite(ByteSource in) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    public URI getFileLocation() {
                        throw new UnsupportedOperationException();
                    }

                    public boolean isExists() {
                        return true;
                    }

                    public boolean isFileBacked() {
                        return false;
                    }

                    public boolean delete() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public String getPath() {
                return "/a/b/c.jpg";
            }

            public boolean isLocal() {
                return true;
            }

            public HashString getHash() {
                return null;
            }

            public FileReference renameTo(FileReference target) throws IOException {
                throw new UnsupportedOperationException();
            }

            public FileReference copyTo(FileReference target) throws IOException {
                throw new UnsupportedOperationException();
            }

            public void unlink() {
                getData().delete();
            }

        };
    }

}
