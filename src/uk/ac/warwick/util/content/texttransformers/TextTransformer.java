package uk.ac.warwick.util.content.texttransformers;

/**
 * Trivial interface which modifies the specified text in some way.
 * 
 * Stateless as this is a single op.
 * 
 * @author xusqac
 */
public interface TextTransformer {
    String transform(final String text);
}
