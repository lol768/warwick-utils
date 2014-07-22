package uk.ac.warwick.util.files.hash.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.ExpectedException;
import uk.ac.warwick.util.AbstractJUnit4FileBasedTest;
import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.FileHasher;
import uk.ac.warwick.util.files.hash.HashString;


public final class FileSystemBackedHashResolverTest extends AbstractJUnit4FileBasedTest {
    
    private final Mockery m = new JUnit4Mockery();
    private final FileHasher hasher = m.mock(FileHasher.class);
    private final HashFileStore store = m.mock(HashFileStore.class);
    private final MaintenanceModeFlags flags = new MaintenanceModeFlags() {
        public boolean isInMaintenanceMode() {
            return false;
        }
    };
    
    private FileSystemBackedHashResolver resolver;
    
    private HashInfoDAO dao;
    
    @Before public void setup() {        
        dao = m.mock(HashInfoDAO.class);
        
        resolver = new FileSystemBackedHashResolver(hasher, FileHashResolver.STORE_NAME_HTML, root, dao, flags);
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void hashNotBelongingToMe() {
        final HashString hash = new HashString("tiftof");
        resolver.lookupByHash(store, hash, true);
    }
    
    /**
     * Check that when we lookup an unqualified hash on the default resolver,
     * the returned HashString doesn't have "default/" or worse, "null/" at
     * the start.
     */
    @Test public void lookupByHashDefault() throws Exception {
        resolver = new FileSystemBackedHashResolver(hasher, FileHashResolver.STORE_NAME_DEFAULT, root, dao, flags);
        
        final String hash = "abcdefghijklmn";
        final HashString hashString = new HashString(hash);
        
        m.checking(new Expectations(){{
            oneOf(dao).getHashByIdWithoutFlush(hashString.toString()); will(returnValue(null));
            oneOf(dao).hashCreated(hashString, 0);
        }});
        
        HashFileReference reference = resolver.lookupByHash(store, hashString, true);
        
        assertEquals(hash, reference.getHash().toString());
    }

    @Test public void lookupByHash() throws Exception {
        // Some illegal chars in hash, but that is fine - we need to make sure it's changed on the actual ref though
        final HashString hash = new HashString(FileHashResolver.STORE_NAME_HTML, "ABCdef_-12345__6--7890&&abcdef1234567890&&abcdef1234567890");
        String expectedPath =
            FilenameUtils.separatorsToSystem(
                    "ab/cd/ef/_-/12/345__6--7890__abcdef1234567890__abcdef1234567890.data"
            );
        
        m.checking(new Expectations(){{
            oneOf(dao).getHashByIdWithoutFlush("html/abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890"); will(returnValue(null));
            oneOf(dao).hashCreated(new HashString("html/abcdef_-12345__6--7890__abcdef1234567890__abcdef1234567890"),0);
        }});
        
        File expectedFile = new File(root, expectedPath); 
        
        HashFileReference reference = resolver.lookupByHash(store, hash, true);
        assertNotNull(reference);
        
        assertEquals(hash, reference.getHash());
        assertFalse(reference.isExists());
        
        assertTrue(reference.isFileBacked());
        assertEquals(expectedFile.getAbsolutePath(), reference.getRealPath());
        
        // now create the file
        expectedFile.getParentFile().mkdirs();
        assertTrue(expectedFile.createNewFile());
        
        // we don't need to fetch the reference again, this one is fine
        assertEquals(hash, reference.getHash());
        assertTrue(reference.isExists());
        
        assertTrue(reference.isFileBacked());
        assertEquals(expectedFile.getAbsolutePath(), reference.getRealPath());
    }

    @Test
    public void generateHash() throws Exception {
        final String hash = "abcdef";
        
        final InputStream is = new ByteArrayInputStream(new byte[0]);
        
        m.checking(new Expectations() {{
            oneOf(hasher).hash(is); will(returnValue(hash));
        }});
        
        HashString generateHash = resolver.generateHash(is);
        assertEquals(hash, generateHash.getHash());
        assertEquals("html", generateHash.getStoreName());
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void partition() throws Exception {
        String hash = "1234567890abcdef1234567890abcdef";
        String expected = FilenameUtils.separatorsToSystem("12/34/56/78/90/abcdef1234567890abcdef.data");
        
        assertEquals(expected, FileSystemBackedHashResolver.partition(hash));
    }
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Test
    public void resolveFilenameMissingDotDataExtension() {
        String filePath =
                FilenameUtils.separatorsToSystem(
                        "ab/cd/ef/_-/12/345__6--7890__abcdef1234567890__abcdef1234567890"
                );

        File file = new File(root, filePath);

        exception.expect(IllegalArgumentException.class);
        resolver.resolve(file, "test");
    }

}
