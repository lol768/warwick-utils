package uk.ac.warwick.util.files.hash.impl;

import org.apache.commons.codec.digest.DigestUtils;

import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.files.hash.MessageAuthenticationCodeGenerator;

/**
 * Implementation of {@link MessageAuthenticationCodeGenerator} that hashes the url params
 * and a salt using the SHA-1 algorithm.
 * 
 */
public final class SHAAuthenticationCodeGenerator implements MessageAuthenticationCodeGenerator {
    
    private final String salt;
    
    public SHAAuthenticationCodeGenerator(String theSalt){
        this.salt = theSalt;
    }

    public String generateMessageAuthenticationCode(String urlParams) {
      
        if (!isValidSalt()){
            return null;
        }
        
        // Append the salt to the the url params
        String concatenatedValues = urlParams.concat(salt);
        return getSHAHash(concatenatedValues);
        
    }

    private String getSHAHash(String input) {
        // backed by Apache Commons-Codec
        return DigestUtils.shaHex(input);
        
    }

    public boolean isValidSalt() {
        return StringUtils.hasText(salt);
    }
}
