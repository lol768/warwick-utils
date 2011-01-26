package uk.ac.warwick.util.web;

/**
 * An injectable interface for parsing Uris out of String text.
 */
public interface UriParser {
    /**
     * Produces a new Uri from a text representation.
     * 
     * @param text
     *            The text uri.
     * @return A new Uri, parsed into components.
     */
    Uri parse(String text);

    /**
     * Returns whether the text representation would try and create an opaque
     * Uri, which is explicitly disallowed.
     */
    boolean isOpaque(String text);
}
