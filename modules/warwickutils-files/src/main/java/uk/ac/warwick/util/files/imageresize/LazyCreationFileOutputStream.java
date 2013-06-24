package uk.ac.warwick.util.files.imageresize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps a FileOutputStream to delay creation of the File until
 * the first write.
 * 
 * The delegating slows things down, so if you're really into performance
 * then it may be better to just delay creating your FileOutputStream
 * until you need it, by passing it in a container resource class or something.
 */
public final class LazyCreationFileOutputStream extends OutputStream {

    private FileOutputStream delegate;
    private final File file;
    
    public LazyCreationFileOutputStream(File f) {
        file = f;
    }
    
    @Override
    public void write(int b) throws IOException {
        getDelegate().write(b);
    }
    
    @Override
    public void write(byte[] b) throws IOException {
        getDelegate().write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getDelegate().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (delegate != null) {
            delegate.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        if (delegate != null) {
            delegate.close();
        }
    }
    
    private FileOutputStream getDelegate() throws FileNotFoundException {
        if (delegate == null) {
            delegate = new FileOutputStream(file);
        }
        return delegate;
    }

}
