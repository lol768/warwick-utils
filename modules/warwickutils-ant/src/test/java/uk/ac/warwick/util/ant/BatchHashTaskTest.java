package uk.ac.warwick.util.ant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTestBase;
import org.springframework.core.io.FileSystemResource;


public class BatchHashTaskTest extends BuildFileTestBase {
    
    @Override
    public void setUp() throws Exception {
        System.setProperty("root", "");
        String root = getClass().getResource("/batchchecksum-build.xml").getPath();
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
    
    @SuppressWarnings("unchecked")
    public void testRun() throws Exception {
        assertFalse(new File("build/statichashes.properties").exists());
        
        executeTarget("run");
        
        Properties props = new Properties();
        props.load(new FileReader(new File(System.getProperty("java.io.tmpdir"), "statichashes.properties")));
        // choice of hashes to allow for cross-platform hashing
        assertThat( props.getProperty("scripts/prototype-1.6.0.3.js"), anyOf(is("991507798479"), is("315463420163")) );
        assertThat( props.getProperty("somecsv.csv"), anyOf(is("165104721170"), is("175649914114")) );
    }
    
}
