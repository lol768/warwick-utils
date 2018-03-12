package uk.ac.warwick.util.files.imageresize;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.AbstractJUnit4FileBasedTest;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy;
import uk.ac.warwick.util.files.LocalFileStore;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHasher;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.hash.impl.BlobStoreBackedHashResolver;
import uk.ac.warwick.util.files.hash.impl.SHAFileHasher;
import uk.ac.warwick.util.files.imageresize.ImageResizer.FileType;
import uk.ac.warwick.util.files.impl.BlobStoreFileStore;
import uk.ac.warwick.util.files.impl.FileBackedHashFileReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.Assert.*;

public class CachingImageResizerTest extends AbstractJUnit4FileBasedTest {

    private static final String CONTAINER_PREFIX = "uk.ac.warwick.sbr.";

    private final Mockery m = new JUnit4Mockery();

    private final FileHasher hasher = new SHAFileHasher();
    private final HashInfoDAO dao = m.mock(HashInfoDAO.class);
    private final BlobStoreContext blobStoreContext = ContextBuilder.newBuilder("transient").buildView(BlobStoreContext.class);
    private final BlobStoreBackedHashResolver resolver = new BlobStoreBackedHashResolver(blobStoreContext, CONTAINER_PREFIX, "resized-images", hasher, dao, () -> false);
    private final FileReferenceCreationStrategy strategy = m.mock(FileReferenceCreationStrategy.class);
    private final Storeable.StorageStrategy storageStrategy = m.mock(Storeable.StorageStrategy.class);
    private final LocalFileStore fileStore = new BlobStoreFileStore(Collections.singletonMap("resized-images", resolver), strategy, blobStoreContext, CONTAINER_PREFIX);

    private final ZonedDateTime now = ZonedDateTime.now();

    private File contentRoot;
    private CachingImageResizer.FileStoreScaledImageCache cache;


    @Before
    public void createCache() throws Exception {
        contentRoot = new File(getRoot(), "content");
        contentRoot.mkdirs();

        m.checking(new Expectations() {{
            ignoring(dao);
            allowing(storageStrategy).getRootPath();
                will(returnValue("resized-images"));
        }});
        resolver.afterPropertiesSet();

        cache = new CachingImageResizer.FileStoreScaledImageCache(fileStore, storageStrategy);
    }

    @After
    public void shutdown() {
        FileUtils.recursiveDelete(contentRoot);
        blobStoreContext.close();
    }
    
    @Test
    public void renderResized() throws Exception {
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(ByteSource in, HashString hash, ZonedDateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(ByteSource in, HashString hash, ZonedDateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        CachingImageResizer resizer = new CachingImageResizer(dummyResizer, cache);
        
        FileReference sourceFile = file("/test/test.jpg");
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);     
        
        assertFalse(cache.contains(sourceFile.getHash(), now, 10, 10));
        
        resizer.renderResized(sourceFile.asByteSource(), sourceFile.getHash(), now, bos, 10, 10, FileType.jpg);
        byte[] result = bos.toByteArray();
        assertEquals(input.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }        
        
        assertTrue(cache.contains(sourceFile.getHash(), now.minusHours(1), 10, 10));
        
        // this time, serve out of the cache
        bos = new ByteArrayOutputStream(input.length);
        resizer.renderResized(sourceFile.asByteSource(), sourceFile.getHash(), now.minusHours(1), bos, 10, 10, FileType.jpg);
        
        result = bos.toByteArray();
        assertEquals(input.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }   
    }
    
    @Test
    public void getResizedImageLength() throws Exception {
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(ByteSource in, HashString hash, ZonedDateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(ByteSource in, HashString hash, ZonedDateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        CachingImageResizer resizer = new CachingImageResizer(dummyResizer, cache);
        
        FileReference sourceFile = file("/test/test.jpg");
        assertEquals(32992L, resizer.getResizedImageLength(sourceFile.asByteSource(), sourceFile.getHash(), now, 10, 10, FileType.jpg));
    }
    
    /* Cache tests */
    
    @Test
    public void scaledImageCacheContains() throws IOException, InterruptedException {
        FileReference nosuch = file("/ab/cd/ef");
        assertFalse(cache.contains(nosuch.getHash(), now.minusHours(1), 10   , 10));
        
        FileReference doesExist = file("/ab/cd/ef");

        // create a fake cache entry for this file
        fileStore.getForPath(storageStrategy, "abcdef@10x10").overwrite(ByteSource.wrap("something".getBytes()));
        
        assertTrue(cache.contains(doesExist.getHash(), now.minusHours(1), 10, 10));
        assertFalse(cache.contains(doesExist.getHash(), now.minusHours(1), 20, 20));
    }
    
    @Test
    public void scaledImageCacheStaleEntry() throws InterruptedException, IOException {
        final ZonedDateTime lastModified = now.plusHours(1);
        
        // create a fake cache entry for this file
        fileStore.getForPath(storageStrategy, "abcdef@10x10").overwrite(ByteSource.wrap("something".getBytes()));

        FileReference doesExist = file("/ab/cd/ef");
        File realFile = new File(doesExist.getFileLocation().getPath());
        realFile.getParentFile().mkdirs();
        realFile.createNewFile();
        
        // should be false since the cache entry is older than the file
        assertFalse(cache.contains(doesExist.getHash(), lastModified,10,10));
        
    }
    
    private FileReference file(String path) throws IOException {
        File f = new File(contentRoot, path);
        FileReference ref = new FileBackedHashFileReference(null, f, new HashString("abcdef"));
        return ref;
    }
    
    @Test
    public void serveFromCache() throws IOException {

        // create a fake cache entry for this file
        ByteSource in = Files.asByteSource(new File(this.getClass().getResource("/tallThinSample.jpg").getFile()));
        fileStore.getForPath(storageStrategy, "abcdef@10x10").overwrite(in);

        FileReference original = file("/test.jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) in.size());
        cache.serveFromCache(original.getHash(), now, bos, 10, 10);
        byte[] result = bos.toByteArray();
        
        assertEquals(in.size(), result.length);

        byte[] input = in.read();
        // byte-for-byte comparison; should be the same file
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }        
    }
    
    @Test
    public void cacheAndServe() throws IOException {
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(ByteSource in, HashString hash, ZonedDateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(ByteSource in, HashString hash, ZonedDateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        FileReference sourceFile = file("/test/test.jpg");
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);        
        
        cache.cacheAndServe(sourceFile.asByteSource(), sourceFile.getHash(), now, bos, 10, 10, FileType.jpg, dummyResizer);
        byte[] result = bos.toByteArray();
        assertEquals(input.length, result.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(input[i], result[i]);
        }        
        
        assertTrue(cache.contains(sourceFile.getHash(), now.minusHours(1), 10, 10));
    }
    
    @Test
    public void getFileSize() throws Exception {
        ImageResizer dummyResizer = new ImageResizer() {
            // whatever the input, write the bytes from tallThinSample.jpg into the output stream
            public void renderResized(ByteSource in, HashString hash, ZonedDateTime lastModified, OutputStream out, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
                out.write(input);
            }

            public long getResizedImageLength(ByteSource in, HashString hash, ZonedDateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
                return FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg")).length;
            }
        };
        
        FileReference sourceFile = file("/test/test.jpg");
        
        assertEquals(32992L, cache.getFileSize(sourceFile.asByteSource(), sourceFile.getHash(), now, 10, 10, FileType.jpg, dummyResizer));
        
        byte[] input = FileCopyUtils.copyToByteArray(this.getClass().getResourceAsStream("/tallThinSample.jpg"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);        
        
        cache.cacheAndServe(sourceFile.asByteSource(), sourceFile.getHash(), now, bos, 10, 10, FileType.jpg, dummyResizer);
        
        assertEquals(32992L, cache.getFileSize(sourceFile.asByteSource(), sourceFile.getHash(), now, 10, 10, FileType.jpg, dummyResizer));
    }
}
