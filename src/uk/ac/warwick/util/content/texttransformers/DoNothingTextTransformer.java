package uk.ac.warwick.util.content.texttransformers;


/**
 * Trivial do-nothing implementation of TextTransformer which returns the
 * text unmodified.
 */
public final class DoNothingTextTransformer implements TextTransformer {
    public String transform(final String text) {
        return text;
    }
}
