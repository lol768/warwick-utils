package uk.ac.warwick.util.files.impl;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Description;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.TransientApiMetadata;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.AbstractJUnit4FileBasedTest;
import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashInfoImpl;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.StaticFileReferenceCreationStrategy;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.files.hash.impl.BlobStoreBackedHashResolver;
import uk.ac.warwick.util.files.hash.impl.SHAFileHasher;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * SBTWO-7780
 */
public class BlobBackedHashFileReferenceLargeStreamsTest extends AbstractJUnit4FileBasedTest {

    private static final String CONTAINER_PREFIX = "uk.ac.warwick.util.";

    private final Mockery m = new JUnit4Mockery();
    private final MaintenanceModeFlags flags = () -> false;
    private final HashInfoDAO sbd = m.mock(HashInfoDAO.class);

    private final BlobStoreContext blobStoreContext =
        ContextBuilder.newBuilder(new TransientApiMetadata())
            .modules(Collections.singleton(new SLF4JLoggingModule()))
            .buildView(BlobStoreContext.class);

    private final FileHashResolver defaultResolver =
        new BlobStoreBackedHashResolver(blobStoreContext, CONTAINER_PREFIX, FileHashResolver.STORE_NAME_DEFAULT, new SHAFileHasher(), sbd, flags);

    private final BlobStoreFileStore fileStore = new BlobStoreFileStore(
        Collections.singletonMap(FileHashResolver.STORE_NAME_DEFAULT, defaultResolver), StaticFileReferenceCreationStrategy.hash(), blobStoreContext, CONTAINER_PREFIX
    );

    @Before
    public void setup() {
        blobStoreContext.getBlobStore().createContainerInLocation(null, "uk.ac.warwick.util.temp");
        blobStoreContext.getBlobStore().createContainerInLocation(null, "uk.ac.warwick.util.default");

        m.checking(new Expectations() {{
            allowing(sbd).getHashByIdWithoutFlush(with(any(String.class))); will(new Action() {
                @Override
                public void describeTo(Description description) {
                    description.appendText("return valid HashInfo");
                }

                @Override
                public Object invoke(Invocation invocation) throws Throwable {
                    String hash = invocation.getParameter(0).toString();

                    return new HashInfoImpl(new HashString(hash), LocalDateTime.now(), 0L);
                }
            });
        }});
    }

    @After
    public void shutdown() {
        blobStoreContext.close();
    }

    @Test
    public void copyLargeBlobsWithStreams() throws Exception {
        // Build a file larger than 50mb
        File file = File.createTempFile("large", ".file", root);
        CharSink cs = Files.asCharSink(file, Charsets.UTF_8, FileWriteMode.APPEND);

        // Write 51mb of data
        for (int i = 0; i < 51; i++) {
            cs.write(RandomStringUtils.randomAscii(1024 * 1024));
        }

        assertEquals(53477376, file.length());

        // Write to an SLO on the store
        LocalFileReference localRef = fileStore.storeLocalReference(new Storeable() {
            @Override
            public String getPath() {
                return "/local-slo";
            }

            @Override
            public HashString getHash() {
                return null;
            }

            @Override
            public StorageStrategy getStrategy() {
                return new StorageStrategy() {
                    @Override
                    public String getRootPath() {
                        return "temp";
                    }

                    @Override
                    public MissingContentStrategy getMissingContentStrategy() {
                        return MissingContentStrategy.Local;
                    }

                    @Override
                    public String getDefaultHashStore() {
                        return null;
                    }

                    @Override
                    public boolean isSupportsLocalReferences() {
                        return true;
                    }
                };
            }
        }, Files.asByteSource(file));

        assertTrue(localRef.isExists());
        assertEquals(file.length(), localRef.length());

        // Use the local file ref to create a hash ref
        HashFileReference hashRef = fileStore.storeHashReference(localRef.asByteSource(), FileHashResolver.STORE_NAME_DEFAULT);

        assertTrue(hashRef.isExists());
        assertEquals(file.length(), hashRef.length());
    }

}