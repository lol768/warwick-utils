package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;


/**
 * Trivial do-nothing implementation of TextTransformer which returns the
 * text unmodified.
 */
public class DoNothingTextTransformer implements TextTransformer {
    public MutableContent apply(MutableContent mc) {
        return mc;
    }
}
