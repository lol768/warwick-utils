package uk.ac.warwick.util.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

/**
 * Takes a set of files and computes some version hashes into a properties file,
 * based on the contents of each file.
 * <p>
 * Although a hashing algorithm is used, the end value is likely modified and truncated
 * so it's only really useful as a cache-busting version identifier. 
 * <p>
 * Example:
 * <pre>
 *  &lt;batchhash propertyfile="/WEB-INF/" maxhashlength="12">
 *    &lt;fileset dir="${static.dir}" includes="**" />
 *  &lt;/batchhash>
 * </pre>
 */
public class BatchHashTask extends Task {
    private List<FileSet> fileSets = new ArrayList<FileSet>();
    
    private File propertyfile;
    private int maxHashLength = -1;
    
    public void addFileSet(FileSet s) {
        fileSets.add(s);
    }
    
    public void setPropertyfile(File f) {
        this.propertyfile = f;
    }
    
    /**
     * If set, property values will be no longer than this
     * number of characters.
     */
    public void setMaxhashlength(int max) {
        this.maxHashLength = max;
    }
    
    @Override
    public void execute() throws BuildException {
        if (fileSets.isEmpty()) {
            throw new BuildException("At least one fileset must be specified");
        }
        if (propertyfile == null) {
            throw new BuildException("propertyfile must be specified");
        }
        
        StringBuilder b = new StringBuilder();
        
        Properties properties = new Properties();
        
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            
            for (FileSet fileSet : fileSets) {
                DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
                File baseDir = ds.getBasedir();
                for (String file : ds.getIncludedFiles()) {
                    String hash = digest(baseDir, file, digester);
                    properties.put(file, hash);
                }
            }
            
            String comment = "Pregenerated static file version identifiers";
            if (maxHashLength > 0) {
                comment += " - max length " + maxHashLength;
            }
            
            properties.store(new FileWriter(propertyfile), comment);
            
        } catch (NoSuchAlgorithmException e) {
            throw new BuildException(e);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
        
        
        
    }

    private String digest(File baseDir, String file, MessageDigest digester) throws UnsupportedEncodingException, IOException {
        digester.reset();

        BufferedInputStream input = new BufferedInputStream(new FileInputStream(new File(baseDir, file)));
        int read = 0;
        byte[] buffer = new byte[8096];
        while((read = input.read(buffer)) > 0) {
            digester.update(buffer, 0, read);
        }
        
        BigInteger integer = new BigInteger(digester.digest());
        // negate any negative numbers
        if (integer.signum() == -1) {
            integer = integer.negate();
        }
        // shorten to a maximum length
        String hash = integer.toString();
        if (maxHashLength > 0 && hash.length() > maxHashLength) {
            hash = hash.substring(0, maxHashLength);
        }
        return hash;
    }
}
