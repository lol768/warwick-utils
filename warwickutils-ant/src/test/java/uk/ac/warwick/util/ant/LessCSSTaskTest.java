package uk.ac.warwick.util.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTestBase;
import org.springframework.core.io.ClassPathResource;

public class LessCSSTaskTest extends BuildFileTestBase {
    
    @Override
    public void setUp() throws Exception {
        System.setProperty("root", "");
        String root = getClass().getResource("/lesscss-build.xml").getPath();
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
    
    public void testRun() throws Exception {
        assertFalse( new ClassPathResource("css-static/edit/style.css").exists() );
        executeTarget("run");
        assertTrue( new ClassPathResource("css-static/edit/style.css").exists() );
    }
    
}
