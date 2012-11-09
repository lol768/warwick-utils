package uk.ac.warwick.util.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import uk.ac.warwick.util.core.spring.FileUtils;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;

public class LessCSSTask extends Task {
    
    private List<FileSet> fileSets = new ArrayList<FileSet>();
    
    private File todir;
    
    public void addFileSet(FileSet s) {
        fileSets.add(s);
    }
    
    @Override
    public void execute() throws BuildException {
        if (fileSets.isEmpty()) {
            throw new BuildException("At least one fileset must be specified, or use dir attribute");
        }
        if (todir == null) {
            throw new BuildException("Must specify todir");
        }
        if (!todir.exists() && !todir.mkdirs()) {
            throw new BuildException("todir can't be created");
        }
        LessEngine engine = new LessEngine();
        
        String currentFilename = null;
        
        try {
            for (FileSet fileSet : fileSets) {
                DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
                File baseDir = ds.getBasedir();
                for (String file : ds.getIncludedFiles()) {
                    currentFilename = file;
                    
                    String sourceFile = baseDir + "/" + file;
                    String newFilename = FileUtils.getFileNameWithoutExtension(file) + ".css";
                    File targetFile = new File(todir, newFilename);
                    targetFile.getParentFile().mkdirs();
                    
                    // for some reason the compiled output has literal \n instead of newlines
                    String compiled = engine.compile(new File(sourceFile)).replace("\\n", "\n");
                    FileWriter f = new FileWriter(targetFile);
                    try {
                        f.write(compiled);
                    } finally {
                        f.close();
                    }
                }
            }
        } catch (LessException e) {
            throw new BuildException(e, new Location(currentFilename));
        } catch (FileNotFoundException e) {
            throw new BuildException(e, new Location(currentFilename));
        } catch (IOException e) {
            throw new BuildException(e, new Location(currentFilename));
        }
    }

    public void setTodir(File todir) {
        this.todir = todir;
    }
    
    /**
     * Convenient shorthand.
     */ 
    public void setDir(File dir) {
        FileSet fs = new FileSet();
        fs.setDir(dir);
        fs.setIncludes("**/*.less");
        this.fileSets.add(fs);
    }
}
