package uk.ac.warwick.util.files.imageresize;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.files.imageresize.JAIImageResizer;
import uk.ac.warwick.util.files.imageresize.SingleThreadedImageResizer;
import uk.ac.warwick.util.files.imageresize.ImageResizer.FileType;
import uk.ac.warwick.util.files.FileData;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileStore.UsingOutput;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.impl.AbstractFileReference;
import uk.ac.warwick.util.files.impl.HashBackedFileReference;


import com.sun.media.jai.codec.ByteArraySeekableStream;

public final class SingleThreadedImageResizerTest {
    
    private final SingleThreadedImageResizer resizer = new SingleThreadedImageResizer(new JAIImageResizer());

    @Test
    public void resizeTallThinImage() throws IOException {
        final DateTime lastModified = new DateTime();
        
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 50;
        int maxHeight = 165;
        resizer.renderResized(ref(input), lastModified, output, maxWidth, maxHeight, FileType.jpg);

        RenderedOp result = JAI.create("stream", new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(maxWidth, result.getWidth());
        assertTrue(maxHeight > result.getHeight());
    }

    /**
     * [SBTWO-1920] We didn't have a test that actually tested maxHeight worked;
     * and it didn't. Test that it does.
     */
    @Test
    public void resizeTallThinImageByHeight() throws IOException {
        final DateTime lastModified = new DateTime();
        
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int maxWidth = 0;
        int maxHeight = 155;
        resizer.renderResized(ref(input), lastModified, output, maxWidth, maxHeight, FileType.jpg);

        RenderedOp result = JAI.create("stream", new ByteArraySeekableStream(output.toByteArray()));
        
        // subsample average avoids black line at the bottom
        assertEquals(154, result.getHeight());
    }

    @Test
    public void resizeShortWideImage() throws IOException {
        final DateTime lastModified = new DateTime();
        
        // shortWide.jpg is 200 x 150px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/shortWideSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref(input), lastModified, output, 50, 165, FileType.jpg);

        RenderedOp result = JAI.create("stream", new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(50, result.getWidth());
    }
    
    /**
     * SBTWO-3196 - we were only testing with byte arrays, meanwhile reading from a file started failing.
     */
    @Test
    public void fileReferenceInput() throws Exception {
        final DateTime lastModified = new DateTime();
        
        File f = new File(this.getClass().getResource("/tallThinSample.jpg").getFile());
        FileReference ref = new HashBackedFileReference(null, f, new HashString("abcdef"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref, lastModified, output, 50, 165, FileType.jpg);
        
        assertEquals(output.size(), resizer.getResizedImageLength(ref, lastModified, 50, 165, FileType.jpg));
    }

    @Test
    public void dontResizeLargerThanOriginal() throws IOException {
        final DateTime lastModified = new DateTime();
        
        // tallThinSample.jpg is 100 x 165 px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref(input), lastModified, output, 150, 200, FileType.jpg);

        RenderedOp result = JAI.create("stream", new ByteArraySeekableStream(output.toByteArray()));
        assertEquals(100, result.getWidth());
        assertEquals(165, result.getHeight());
        
        assertEquals("Output should be the same as input", input.length,output.toByteArray().length);
    }
    
    @Test
    public void PNGResizing() throws IOException {
        final DateTime lastModified = new DateTime();
        
        // award.png is 220x233px
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/award.png"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resizer.renderResized(ref(input), lastModified, output, 110, 116, FileType.png);

        RenderedOp result = JAI.create("stream", new ByteArraySeekableStream(output.toByteArray()));
        
        // subsample average avoids black line at the bottom
        assertEquals(109, result.getWidth());
        assertEquals(116, result.getHeight());
    }
    
    private FileReference ref(final byte[] input) {
        return new AbstractFileReference() {

            public FileData getData() {
                return new FileData() {
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(input);
                    }
                    
                    public InputStreamSource getInputStreamSource() {
                        return new InputStreamSource() {
                            public InputStream getInputStream() {
                                return new ByteArrayInputStream(input);
                            }
                        };
                    }
                    
                    public boolean isExists() {
                        return true;
                    }

                    public boolean isFileBacked() {
                        return false;
                    }

                    public long length() {
                        return input.length;
                    }
                    
                    public boolean delete() {
                        throw new UnsupportedOperationException();
                    }

                    public File getRealFile() {
                        throw new UnsupportedOperationException();
                    }

                    public String getRealPath() {
                        throw new UnsupportedOperationException();
                    }

                    public HashString overwrite(UsingOutput callback) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    public HashString overwrite(File file) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    public HashString overwrite(byte[] contents) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    public HashString overwrite(String contents) throws IOException {
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