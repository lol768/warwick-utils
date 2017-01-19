package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.impl.BlobStoreBackedHashResolver;
import uk.ac.warwick.util.files.hash.impl.SHAFileHasher;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BlobStoreFileStoreTest {

    private static final String CONTAINER_PREFIX = "uk.ac.warwick.sbr.";
    
    private static final byte[] DATA = "Hello".getBytes();

    private final Mockery m = new JUnit4Mockery();
    private final MaintenanceModeFlags flags = () -> false;
    private final HashInfoDAO sbd = m.mock(HashInfoDAO.class);

    private final BlobStoreContext blobStoreContext = ContextBuilder.newBuilder("transient").buildView(BlobStoreContext.class);

    private final BlobStoreBackedHashResolver resolver = new BlobStoreBackedHashResolver(blobStoreContext, CONTAINER_PREFIX, FileHashResolver.STORE_NAME_HTML, new SHAFileHasher(), sbd, flags);
    private final FileReferenceCreationStrategy strategy = m.mock(FileReferenceCreationStrategy.class);

    private final BlobStoreFileStore fileStore = new BlobStoreFileStore(Collections.singletonMap(FileHashResolver.STORE_NAME_HTML, resolver), strategy, blobStoreContext, CONTAINER_PREFIX);
    
    @Before public void setUpHere() throws Exception {
        m.checking(new Expectations() {{
            ignoring(sbd);
        }});

        resolver.afterPropertiesSet();
    }
    
    @After public void tearDownHere() throws Exception {
        blobStoreContext.close();
    }
    
    @Test public void storeHashHtmlFile() throws Exception {
        final Storeable s = m.mock(Storeable.class);
        final StorageStrategy ss = m.mock(StorageStrategy.class);
        
        m.checking(new Expectations(){{
            oneOf(strategy).select(with(any(ByteSource.class))); will(returnValue(FileReferenceCreationStrategy.Target.hash));
            allowing(s).getPath(); will(returnValue("/file.htm"));
            allowing(s).getStrategy(); will(returnValue(ss));
            allowing(ss).getRootPath(); will(returnValue(FileHashResolver.STORE_NAME_HTML));
        }});

        FileReference ref = fileStore.store(s, FileHashResolver.STORE_NAME_HTML, ByteSource.wrap(DATA));

        Blob blob = blobStoreContext.getBlobStore().getBlob("uk.ac.warwick.sbr.html", "f7ff9e8b7bb2e09b70935a5d785e0cc5d9d0abf0");
        assertNotNull(blob);
        assertEquals("Hello", FileCopyUtils.copyToString(new InputStreamReader(blob.getPayload().openStream())));

        // Test fetching
        assertEquals("Hello", ref.asByteSource().asCharSource(StandardCharsets.UTF_8).read());
        assertEquals("el", ref.asByteSource().slice(1, 2).asCharSource(StandardCharsets.UTF_8).read());
        
        m.assertIsSatisfied();
    }

    @Test public void storeLocalFile() throws Exception {
        final Storeable s = m.mock(Storeable.class);
        final StorageStrategy ss = m.mock(StorageStrategy.class);

        m.checking(new Expectations(){{
            oneOf(strategy).select(with(any(ByteSource.class))); will(returnValue(FileReferenceCreationStrategy.Target.local));
            allowing(s).getPath(); will(returnValue("/file.htm"));
            allowing(s).getStrategy(); will(returnValue(ss));
            allowing(ss).getRootPath(); will(returnValue(FileHashResolver.STORE_NAME_HTML));
        }});

        FileReference ref = fileStore.store(s, FileHashResolver.STORE_NAME_HTML, ByteSource.wrap(DATA));

        Blob blob = blobStoreContext.getBlobStore().getBlob("uk.ac.warwick.sbr.html", "/file.htm");
        assertNotNull(blob);
        assertEquals("Hello", FileCopyUtils.copyToString(new InputStreamReader(blob.getPayload().openStream())));

        // Test fetching
        assertEquals("Hello", ref.asByteSource().asCharSource(StandardCharsets.UTF_8).read());
        assertEquals("el", ref.asByteSource().slice(1, 2).asCharSource(StandardCharsets.UTF_8).read());

        assertEquals(Collections.singletonList("/file.htm"), fileStore.list(ss, "").collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), fileStore.list(ss, "dir").collect(Collectors.toList()));

        m.assertIsSatisfied();
    }
}
