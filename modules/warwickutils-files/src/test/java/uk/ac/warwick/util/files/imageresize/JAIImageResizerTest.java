package uk.ac.warwick.util.files.imageresize;

import com.sun.media.jai.codec.ByteArraySeekableStream;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.imageresize.ImageResizer.FileType;
import uk.ac.warwick.util.files.impl.FileBackedHashFileReference;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;

import static org.junit.Assert.*;

public final class JAIImageResizerTest {

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
        resizer.renderResized(input, output, 300, 300, FileType.jpg);
    }
    
    @Test
    public void resizeTallThinImage() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 50;
        int maxHeight = 165;
        resizer.renderResized(input, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
        assertEquals(maxWidth, result.getWidth());
        assertTrue(maxHeight > result.getHeight());
    }
    
    @Test
    public void resizeAsFileNotByteArray() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();

        // tallThinSample.jpg is 100 x 165 px
        File input = File.createTempFile("tallThinSample", ".jpg");
        input.deleteOnExit();
        FileCopyUtils.copy(this.getClass().getResourceAsStream("/tallThinSample.jpg"), new FileOutputStream(input));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 50;
        int maxHeight = 165;
        resizer.renderResized(input, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
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
        int maxWidth = 0;
        int maxHeight = 155;
        resizer.renderResized(input, output, maxWidth, maxHeight, FileType.jpg);

        PlanarImage result = PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
        
        // subsample average avoids black line at the bottom
        assertNotEquals("Image hasn't been resized at all", 165, result.getHeight());
        assertEquals("Image hasn't been resized to the right height", 154, result.getHeight());
    }

    @Test
    public void resizeShortWideImage() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // shortWide.jpg is 200 x 150px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/shortWideSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(input, output, 50, 165, FileType.jpg);

        PlanarImage result = PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
        assertEquals(50, result.getWidth());
    }
    
    /**
     * SBTWO-3196 - we were only testing with byte arrays, meanwhile reading from a file started failing.
     */
    @Test
    public void fileReferenceInput() throws Exception {
        final Instant lastModified = Instant.now();
        
        JAIImageResizer resizer = new JAIImageResizer();

        File f = File.createTempFile("tallThinSample", ".jpg");
        f.deleteOnExit();
        FileCopyUtils.copy(this.getClass().getResourceAsStream("/tallThinSample.jpg"), new FileOutputStream(f));

        FileReference ref = new FileBackedHashFileReference(null, f, new HashString("abcdef"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref, lastModified, output, 50, 165, FileType.jpg);
        
        assertEquals(output.size(), resizer.getResizedImageLength(ref, lastModified, 50, 165, FileType.jpg));
    }

    @Test
    public void dontResizeLargerThanOriginal() throws IOException {
        JAIImageResizer resizer = new JAIImageResizer();
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(input, output, 150, 200, FileType.jpg);

        PlanarImage result = PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
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
        resizer.renderResized(input, output, 110, 116, FileType.png);

        PlanarImage result = PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
        
        // subsample average avoids black line at the bottom
        assertEquals(109, result.getWidth());
        assertEquals(116, result.getHeight());
    }
    
    @Test
    public void resizeTheWorld() throws Exception {
        JAIImageResizer resizer = new JAIImageResizer();

        File input = File.createTempFile("pendulum_crop1", ".jpg");
        input.deleteOnExit();
        FileCopyUtils.copy(this.getClass().getResourceAsStream("/pendulum_crop1.jpg"), new FileOutputStream(input));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 350;
        int maxHeight = 149;
        resizer.renderResized(input, output, maxWidth, maxHeight, FileType.jpg);

        // Image resizer will refuse to resize.
        Pair<Integer, Integer> dimensions = JAIImageResizer.getDimensions(new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(24803, dimensions.getLeft().intValue());
        assertEquals(10559, dimensions.getRight().intValue());
    }    
    
    @Test
    @Ignore("Brittle")
    public void badImage() throws Exception {
        final Instant lastModified = Instant.now();
        
        JAIImageResizer resizer = new JAIImageResizer();

        File f = File.createTempFile("October", ".jpg");
        f.deleteOnExit();
        FileCopyUtils.copy(this.getClass().getResourceAsStream("/October.jpg"), new FileOutputStream(f));

        FileReference ref = new FileBackedHashFileReference(null, f, new HashString("abcdef"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref, lastModified, output, 50, 165, FileType.jpg);
        
        assertEquals(output.size(), resizer.getResizedImageLength(ref, lastModified, 50, 165, FileType.jpg));
        
        Pair<Integer, Integer> dimensions = JAIImageResizer.getDimensions(new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(1181, dimensions.getLeft().intValue());
        assertEquals(598, dimensions.getRight().intValue());
    }

}
