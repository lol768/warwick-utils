package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;

import static org.junit.Assert.*;

public class EmptyHashBackedFileReferenceTest {

    private final Mockery m = new JUnit4Mockery();

    private final HashFileStore fileStore = m.mock(HashFileStore.class);
    private final HashFileReference hashRef = m.mock(HashFileReference.class);
    private final static String STORE_NAME = "fake-bucket-for-test-files";
    private final static byte[] DATA = "Hello".getBytes();

    @Test
    public void testEmptyFileReference() throws Exception {
        final EmptyHashBackedFileReference emptyRef = new EmptyHashBackedFileReference(fileStore, STORE_NAME);
        final ByteSource bs = ByteSource.wrap(DATA);

        assertFalse(emptyRef.isExists());
        assertFalse(emptyRef.isFileBacked());
        assertFalse(emptyRef.isLocal());
        assertNull(emptyRef.getHash());
        assertNull(emptyRef.getPath());
        assertEquals(0L, emptyRef.length());
        assertEquals(emptyRef, emptyRef.renameTo(hashRef)); // renames to itself
        assertNotEquals(emptyRef, emptyRef.copyTo(hashRef)); // renames to different empty ref
        assertEquals("EmptyHashBackedFileReference", emptyRef.copyTo(hashRef).getClass().getSimpleName());

        m.checking(new Expectations() {{
            final Sequence seq = m.sequence("hashRefSequence");
            one(fileStore).createHashReference(bs, STORE_NAME); inSequence(seq); will(returnValue(hashRef));

        }});

        FileReference newRef = emptyRef.overwrite(bs);
        assertEquals(hashRef, newRef); // overwrite returns new hash reference with the expected data
        m.assertIsSatisfied();
    }
}