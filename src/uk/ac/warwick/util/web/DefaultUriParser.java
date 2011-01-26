package uk.ac.warwick.util.web;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Uri parser using java.net.URI as its basis, enforcing RFC 2396 restrictions.
 */
public class DefaultUriParser implements UriParser {

    /**
     * Produces a new Uri from a text representation.
     * 
     * @param text
     *            The text uri.
     * @return A new Uri, parsed into components.
     */
    public Uri parse(String text) {
        try {
            return Uri.fromJavaUri(new URI(text));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isOpaque(String text) {
        try {
            return new URI(text).isOpaque();
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
