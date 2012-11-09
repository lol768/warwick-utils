package uk.ac.warwick.util.files;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.files.FileStore.UsingOutput;

/**
 * Useful adapter where a method takes a UsingOutput argument and you
 * simply want to copy an InputStream or File directly
 * into it.
 */
public final class CopyToOutput implements UsingOutput, Closeable {

private InputStream is;
    
    public CopyToOutput(InputStream input) {
        Assert.notNull(input);
        this.is = input;
    }
    
    public CopyToOutput(File input) throws FileNotFoundException {
        Assert.notNull(input);
        this.is = new FileInputStream(input);
    }
    
    public CopyToOutput(FileReference ref) throws IOException {
        this.is = ref.getInputStream();
    }
    
    public CopyToOutput(String input) {
        this(StringUtils.create(input));
    }
    
    public CopyToOutput(byte[] input) {
        this.is = new ByteArrayInputStream(input);
    }
    
    public void doWith(OutputStream output) throws IOException {
        FileCopyUtils.copy(is, output);
    }

    public void close() throws IOException {
        is.close();        
    }

}
