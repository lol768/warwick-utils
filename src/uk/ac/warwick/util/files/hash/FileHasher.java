package uk.ac.warwick.util.files.hash;

import java.io.IOException;
import java.io.InputStream;

public interface FileHasher {

    String hash(InputStream is) throws IOException;
    
}
