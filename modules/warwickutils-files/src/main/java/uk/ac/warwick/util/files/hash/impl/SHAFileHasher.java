package uk.ac.warwick.util.files.hash.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;

import uk.ac.warwick.util.files.hash.FileHasher;

public final class SHAFileHasher implements FileHasher {
    
    /* Uses commons-digest */
    public String hash(InputStream is) throws IOException {
        try {
            return DigestUtils.shaHex(is);
        } finally {
            is.close();
        }
    }

}
