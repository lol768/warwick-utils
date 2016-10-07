package uk.ac.warwick.util.files.imageresize;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.AbstractJUnit4FileBasedTest;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.imageresize.ImageResizer.FileType;
import uk.ac.warwick.util.files.impl.FileBackedHashFileReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.*;

public class CachingImageResizerTest extends AbstractJUnit4FileBasedTest {

    private File cacheRoot;
    private File contentRoot;
    private ScaledImageCache cache;

    @Before
    public void createCache() {
        cacheRoot = new File(getRoot(),"cache");
        contentRoot = new File(getRoot(),"content");
        cacheRoot.mkdir();
        contentRoot.mkdirs();
        cache = new FileSystemScaledImageCache(cacheRoot, "@");
    }
    
    @Test
    public void renderResized() throws Exception {
        final DateTime lastModified = new DateTime();
        
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(FileReference sourceFile, DateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(FileReference sourceFile, DateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        CachingImageResizer resizer = new CachingImageResizer(dummyResizer, cache);
        
        FileReference sourceFile = file("/test/test.jpg");        
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);     
        
        assertFalse(cache.contains(sourceFile, lastModified, 10, 10));
        
        resizer.renderResized(sourceFile, lastModified, bos, 10, 10, FileType.jpg);
        byte[] result = bos.toByteArray();
        assertEquals(input.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }        
        
        assertTrue(cache.contains(sourceFile, lastModified.minusHours(1), 10, 10));
        
        // this time, serve out of the cache
        bos = new ByteArrayOutputStream(input.length);
        resizer.renderResized(sourceFile, lastModified.minusHours(1), bos, 10, 10, FileType.jpg);
        
        result = bos.toByteArray();
        assertEquals(input.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }   
    }
    
    @Test
    public void getResizedImageLength() throws Exception {
        final DateTime lastModified = new DateTime();
        
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(FileReference sourceFile, DateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(FileReference sourceFile, DateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        CachingImageResizer resizer = new CachingImageResizer(dummyResizer, cache);
        
        FileReference sourceFile = file("/test/test.jpg");        
        assertEquals(32992L, resizer.getResizedImageLength(sourceFile, lastModified, 10, 10, FileType.jpg));
    }
    
    /* Cache tests */
    
    @Test
    public void scaledImageCacheContains() throws IOException, InterruptedException {
        final DateTime lastModified = new DateTime();
        
        FileReference nosuch = file("/ab/cd/ef.data"); 
        assertFalse(cache.contains(nosuch, lastModified.minusHours(1), 10   , 10));
        
        FileReference doesExist = file("/ab/cd/ef.data");
        // create a fake cache entry for this file
        File doesExistCacheEntry = new File (cacheRoot,"/ab/cd/ef.data@10x10");
        doesExistCacheEntry.getParentFile().mkdirs();
        doesExistCacheEntry.createNewFile();
        
        assertTrue(cache.contains(doesExist, lastModified.minusHours(1),10,10));
        assertFalse(cache.contains(doesExist, lastModified.minusHours(1), 20, 20));
    }
    
    @Test
    public void scaledImageCacheStaleEntry() throws InterruptedException, IOException {
        final DateTime lastModified = new DateTime().plusHours(1);
        
        // create a fake cache entry for this file
        File doesExistCacheEntry = new File (cacheRoot,"/ab/cd/ef.data@10x10");
        doesExistCacheEntry.getParentFile().mkdirs();
        doesExistCacheEntry.createNewFile();

        FileReference doesExist = file("/ab/cd/ef.data");
        File realFile = new File(doesExist.getFileLocation().getPath());
        realFile.getParentFile().mkdirs();
        realFile.createNewFile();
        
        // should be false since the cache entry is older than the file
        assertFalse(cache.contains(doesExist, lastModified,10,10));
        
    }
    
    private FileReference file(String path) throws IOException {
        File f = new File(contentRoot, path);
        FileReference ref = new FileBackedHashFileReference(null, f, new HashString("abcdef"));
        return ref;
    }
    
    @Test
    public void serveFromCache() throws IOException {
        final DateTime lastModified = new DateTime();
                
        //get a real live jpeg file, and copy it into the cache
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        File out = new File(cacheRoot,"ab/cd/ef.data@10x10");
        out.getParentFile().mkdirs();
        FileCopyUtils.copy(input, out);
        
        FileReference original = file("/test.jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        cache.serveFromCache(original, lastModified, bos, 10, 10);
        byte[] result = bos.toByteArray();
        
        assertEquals(input.length, result.length);
        // byte-for-byte comparison; should be the same file
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }        
    }
    
    @Test
    public void cacheAndServe() throws IOException {
        final DateTime lastModified = new DateTime();
        
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(FileReference sourceFile, DateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(FileReference sourceFile, DateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        FileReference sourceFile = file("/test/test.jpg");
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);        
        
        cache.cacheAndServe(sourceFile, lastModified, bos, 10, 10, FileType.jpg, dummyResizer);
        byte[] result = bos.toByteArray();
        assertEquals(input.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }        
        
        assertTrue(cache.contains(sourceFile, lastModified.minusHours(1), 10, 10));
    }
    
    @Test
    public void getFileSize() throws Exception {
        final DateTime lastModified = new DateTime();
        
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(FileReference sourceFile, DateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(FileReference sourceFile, DateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        FileReference sourceFile = file("/test/test.jpg");
        
        assertEquals(32992L, cache.getFileSize(sourceFile, lastModified, 10, 10, FileType.jpg, dummyResizer));
        
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);        
        
        cache.cacheAndServe(sourceFile, lastModified, bos, 10, 10, FileType.jpg, dummyResizer);
        
        assertEquals(32992L, cache.getFileSize(sourceFile, lastModified, 10, 10, FileType.jpg, dummyResizer));
    }
}
