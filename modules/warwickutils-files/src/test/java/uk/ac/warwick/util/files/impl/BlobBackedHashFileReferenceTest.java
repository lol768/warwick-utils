package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.io.Payload;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.files.DefaultFileStoreStatistics;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class BlobBackedHashFileReferenceTest {

    private final Mockery m = new JUnit4Mockery();

    private final HashFileStore fileStore = m.mock(HashFileStore.class);
    private final BlobStore blobStore = m.mock(BlobStore.class);
    private final String containerName = "uk.ac.warwick.sbr.files";
    private final HashString hash = new HashString("files", "62139898312baed7981327ab9812f37");

    private BlobBackedHashFileReference ref;

    @Before
    public void setup() throws Exception {
        m.checking(new Expectations() {{
            allowing(fileStore).getStatistics(); will(returnValue(new DefaultFileStoreStatistics(fileStore)));
        }});

        ref = new BlobBackedHashFileReference(fileStore, blobStore, containerName, hash);
    }

    @Test
    public void lazyBlob() throws Exception {
        final Blob blob = m.mock(Blob.class);
        final MutableBlobMetadata metadata = m.mock(MutableBlobMetadata.class);
        final Payload payload = m.mock(Payload.class);

        // Methods that can be called without the blob being fetched
        assertFalse(ref.isLocal());
        assertFalse(ref.isFileBacked());
        assertEquals(hash, ref.getHash());
        assertNull(ref.getPath());
        assertNotNull(ref.toString());

        m.checking(new Expectations() {{
            one(blobStore).getBlob(containerName, hash.getHash(), GetOptions.NONE); will(returnValue(blob));

            allowing(blob).getMetadata(); will(returnValue(metadata));
            allowing(metadata).getSize(); will(returnValue(12345L));
            one(blob).getPayload(); will(returnValue(payload));
            one(payload).openStream(); will(returnValue(new ByteArrayInputStream(new byte[0])));
        }});

        // Methods that can all be called without a further blob being fetched
        assertTrue(ref.isExists());
        assertEquals(12345L, ref.length());
        assertFalse(ref.asByteSource().isEmpty());
        assertNotNull(ref.asByteSource().openStream());
        assertEquals(12345L, ref.asByteSource().size());

        m.assertIsSatisfied();

        // This requires the blob to be re-fetched to read the payload again
        m.checking(new Expectations() {{
            one(blobStore).getBlob(containerName, hash.getHash(), GetOptions.NONE); will(returnValue(blob));
            one(blob).getPayload(); will(returnValue(payload));
            one(payload).openStream(); will(returnValue(new ByteArrayInputStream(new byte[0])));
        }});

        assertNotNull(ref.asByteSource().openStream());

        m.assertIsSatisfied();

        // Slicing the byte source means that the blob is re-fetched with new params
        final ByteSource slicedByteSource = ref.asByteSource().slice(10, 20);

        final Blob slicedBlob = m.mock(Blob.class, "slicedBlob");
        final Payload slicedBlobPayload = m.mock(Payload.class, "slicedBlobPayload");

        m.checking(new Expectations() {{
            one(blobStore).getBlob(containerName, hash.getHash(), GetOptions.Builder.range(10, 29));  will(returnValue(slicedBlob));
            one(slicedBlob).getPayload(); will(returnValue(slicedBlobPayload));
            one(slicedBlobPayload).openStream(); will(returnValue(new ByteArrayInputStream(new byte[0])));
        }});

        assertFalse(slicedByteSource.isEmpty());
        assertNotNull(slicedByteSource.openStream());
        assertEquals(20L, slicedByteSource.size());

        m.assertIsSatisfied();
    }

}