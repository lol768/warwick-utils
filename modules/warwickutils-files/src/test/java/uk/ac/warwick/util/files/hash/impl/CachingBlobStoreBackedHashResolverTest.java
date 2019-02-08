package uk.ac.warwick.util.files.hash.impl;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.files.DefaultFileStoreStatistics;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.FileHasher;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public final class CachingBlobStoreBackedHashResolverTest {

    private static final String CONTAINER_PREFIX = "uk.ac.warwick.sbr.";
    
    private final Mockery m = new JUnit4Mockery();
    private final FileHasher hasher = m.mock(FileHasher.class);
    private final HashFileStore store = m.mock(HashFileStore.class);
    private final HashInfoDAO dao = m.mock(HashInfoDAO.class);
    private final MaintenanceModeFlags flags = () -> false;

    private final BlobStoreContext blobStoreContext = ContextBuilder.newBuilder("transient").buildView(BlobStoreContext.class);
    
    private final BlobStoreBackedHashResolver underlyingHtmlResolver = new BlobStoreBackedHashResolver(blobStoreContext, CONTAINER_PREFIX, FileHashResolver.STORE_NAME_HTML, hasher, dao, flags);
    private final CachingBlobStoreBackedHashResolver cachingHtmlResolver = new CachingBlobStoreBackedHashResolver(underlyingHtmlResolver);
    private final BlobStoreBackedHashResolver defaultResolver = new BlobStoreBackedHashResolver(blobStoreContext, CONTAINER_PREFIX, FileHashResolver.STORE_NAME_DEFAULT, hasher, dao, flags);

    @Before public void setup() throws Exception {
        underlyingHtmlResolver.afterPropertiesSet();
        defaultResolver.afterPropertiesSet();

        cachingHtmlResolver.setMaximumSizeAsPercentage(25);
        cachingHtmlResolver.afterPropertiesSet();

        m.checking(new Expectations() {{
            allowing(store).getStatistics(); will(returnValue(new DefaultFileStoreStatistics(store)));
        }});
    }

    @After
    public void shutdown() {
        blobStoreContext.close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void hashNotBelongingToMe() {
        final HashString hash = new HashString("tiftof");
        cachingHtmlResolver.lookupByHash(store, hash, true);
    }
    
    /**
     * Check that when we lookup an unqualified hash on the default resolver,
     * the returned HashString doesn't have "default/" or worse, "null/" at
     * the start.
     */
    @Test public void lookupByHashDefault() throws Exception {
        final String hash = "abcdefghijklmn";
        final HashString hashString = new HashString(hash);
        
        m.checking(new Expectations(){{
            oneOf(dao).getHashByIdWithoutFlush(hashString.toString()); will(returnValue(null));
            oneOf(dao).hashCreated(hashString, 0);
        }});
        
        HashFileReference reference = defaultResolver.lookupByHash(store, hashString, true);
        
        assertEquals(hash, reference.getHash().toString());
    }

    @Test public void lookupByHash() throws Exception {
        // Some illegal chars in hash, but that is fine - we need to make sure it's changed on the actual ref though
        final HashString hash = new HashString(FileHashResolver.STORE_NAME_HTML, "abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890");

        m.checking(new Expectations(){{
            oneOf(dao).getHashByIdWithoutFlush("html/abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890"); will(returnValue(null));
            oneOf(dao).hashCreated(new HashString("html/abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890"),0);
        }});

        HashFileReference reference = cachingHtmlResolver.lookupByHash(store, hash, true);
        assertNotNull(reference);
        
        assertEquals(hash, reference.getHash());
        assertFalse(reference.isExists());
        assertFalse(reference.isFileBacked());

        // now create the file
        blobStoreContext.getBlobStore().putBlob(
            "uk.ac.warwick.sbr.html",
            blobStoreContext.getBlobStore().blobBuilder("abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890")
                .payload("File contents")
                .contentDisposition("abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890")
                .contentLength(13L)
                .build()
        );
        
        // we don't need to fetch the reference again, this one is fine
        assertEquals(hash, reference.getHash());
        assertTrue(reference.isExists());
        assertFalse(reference.isFileBacked());
    }

    @Test
    public void generateHash() throws Exception {
        final String hash = "abcdef";
        
        final InputStream is = new ByteArrayInputStream(new byte[0]);
        
        m.checking(new Expectations() {{
            oneOf(hasher).hash(is); will(returnValue(hash));
        }});
        
        HashString generateHash = cachingHtmlResolver.generateHash(is);
        assertEquals(hash, generateHash.getHash());
        assertEquals("html", generateHash.getStoreName());
        
        m.assertIsSatisfied();
    }

}
