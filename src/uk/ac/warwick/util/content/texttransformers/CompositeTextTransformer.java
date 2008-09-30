package uk.ac.warwick.util.content.texttransformers;

import java.util.List;

/**
 * Simple composite TextTransformer which takes a list of other TextTransformers,
 * and executes them in order when transform() is called. Useful when you wish to
 * perform multiple transformations on an object which accepts a single transformer
 * object.
 */
public class CompositeTextTransformer implements TextTransformer {

    private List<TextTransformer> textTransformers;
    
    public CompositeTextTransformer(final List<TextTransformer> theTextTransformers) {
        textTransformers = theTextTransformers;
    }
 
    public final String transform(final String text) {
        String transformedText = text;
        for (TextTransformer transformer : textTransformers) {
            transformedText = transformer.transform(transformedText);
        }
        return transformedText;
    }
}
