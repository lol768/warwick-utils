package uk.ac.warwick.util.files.hash;

/**
 * A UrlParamsMACGenerator is used to generate a short reproducible (one-way) hash of
 * the url params (String) and a salt
 */
public interface MessageAuthenticationCodeGenerator {

    /**
     * Generate the Message Authentication Code for this query String.
     */
    String generateMessageAuthenticationCode(String urlParams);
    
    boolean isValidSalt();

}
