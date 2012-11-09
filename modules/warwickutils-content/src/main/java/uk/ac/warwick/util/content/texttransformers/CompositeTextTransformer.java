package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;

import com.google.common.collect.ImmutableList;

/**
 * Simple composite TextTransformer which takes a list of other TextTransformers,
 * and executes them in order when transform() is called. Useful when you wish to
 * perform multiple transformations on an object which accepts a single transformer
 * object.
 */
public class CompositeTextTransformer implements TextTransformer {

    private final ImmutableList<TextTransformer> textTransformers;
    
    public CompositeTextTransformer(final Iterable<? extends TextTransformer> theTextTransformers) {
        this.textTransformers = ImmutableList.copyOf(theTextTransformers);
    }
 
    public final MutableContent apply(MutableContent mc) {
        for (TextTransformer transformer : textTransformers) {
            mc = transformer.apply(mc);
        }
        return mc;
    }
}
