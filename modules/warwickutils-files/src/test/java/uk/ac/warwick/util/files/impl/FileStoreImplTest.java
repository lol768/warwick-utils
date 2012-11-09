package uk.ac.warwick.util.files.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.AbstractJUnit4FileBasedTest;
import uk.ac.warwick.util.core.MaintenanceModeFlags;
import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.CopyToOutput;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.Storeable.StorageStrategy;
import uk.ac.warwick.util.files.dao.HashInfoDAO;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.impl.FileSystemBackedHashResolver;
import uk.ac.warwick.util.files.hash.impl.SHAFileHasher;

public class FileStoreImplTest extends AbstractJUnit4FileBasedTest {
    
    private static final byte[] DATA = "Hello".getBytes();
    
    private FileStoreImpl fileStore;
    private TemporaryUploadsDirectoryHolder props;
    private MaintenanceModeFlags flags;
    private FileHashResolver resolver;
    private FileReferenceCreationStrategy strategy;
    private Mockery m = new Mockery();
    
    private File tempDir;
    
    @Before public void setUpHere() throws Exception {
        tempDir = File.createTempFile("aaa", "bbb");
        tempDir.delete();
        tempDir.mkdir();
        
        props = new TemporaryUploadsDirectoryHolder() {
            public File getTemporaryUploadedFileDirectory() {
                return tempDir;
            }
        };
        
        flags = new MaintenanceModeFlags() {
            public boolean isInMaintenanceMode() {
                return false;
            }
        };
        
        strategy = m.mock(FileReferenceCreationStrategy.class);
        
        final HashInfoDAO sbd = m.mock(HashInfoDAO.class);
        m.checking(new Expectations() {{
            ignoring(sbd);
        }});
        
        resolver = new FileSystemBackedHashResolver(new SHAFileHasher(), FileHashResolver.STORE_NAME_HTML, getRoot(), sbd, flags);
                
        fileStore = new FileStoreImpl(props, Collections.singletonMap(FileHashResolver.STORE_NAME_HTML, resolver), strategy);
    }
    
    @After public void tearDownHere() throws Exception {
        FileUtils.recursiveDelete(tempDir);
        tempDir.deleteOnExit();
    }
    
    @Test public void storeHashHtmlFile() throws Exception {
        final Storeable s = m.mock(Storeable.class);
        final StorageStrategy ss = m.mock(StorageStrategy.class);
        
        m.checking(new Expectations(){{
            oneOf(strategy).select(with(any(File.class))); will(returnValue(FileReferenceCreationStrategy.Target.hash));
            allowing(s).getPath(); will(returnValue("/file.htm"));
            allowing(s).getStrategy(); will(returnValue(ss));
        }});
        
        fileStore.store(s, FileHashResolver.STORE_NAME_HTML, new CopyToOutput(new ByteArrayInputStream(DATA)));
        
        File createdFile = new File(getRoot(), "f7/ff/9e/8b/7b/b2e09b70935a5d785e0cc5d9d0abf0.data");
        assertEquals(new String(DATA), FileCopyUtils.copyToString(new FileReader(createdFile)));
        
        m.assertIsSatisfied();
    }
}
