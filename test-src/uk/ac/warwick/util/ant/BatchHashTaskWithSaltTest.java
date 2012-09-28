package uk.ac.warwick.util.ant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTestBase;
import org.springframework.core.io.FileSystemResource;


public class BatchHashTaskWithSaltTest extends BuildFileTestBase {
    
    @Override
    public void setUp() throws Exception {
        System.setProperty("root", "");
        String root = getClass().getResource("/batchchecksum-withsalt-build.xml").getPath();
        //System.err.println(root);
        configureProject(root);
    }
    
    @Override
    public void tearDown() throws Exception {
        executeTarget("cleanup");
    }
    
    public void testMissingDir() {
        try {
            executeTarget("missing-fileset");
            fail();
        } catch (BuildException e) {
        }
    }
    
    public void testMissingTarget() {
        try {
            executeTarget("missing-propertyfile");
            fail();
        } catch (BuildException e) {
        }
    }
    
    public void testRun() throws Exception {
        assertFalse( new FileSystemResource("build/statichashes.properties").exists() );
        
        executeTarget("run");
        
        Properties props = new Properties();
        props.load(new FileReader(new File(System.getProperty("java.io.tmpdir"), "statichashes.properties")));
        // choice of hashes to allow for cross-platform hashing
        assertThat( props.getProperty("scripts/prototype-1.6.0.3.js"), anyOf(is("168556932232"), is("359133207138")) );
        assertThat( props.getProperty("somecsv.csv"), anyOf(is("131825934230"), is("119441393854")) );
    }
    
}
