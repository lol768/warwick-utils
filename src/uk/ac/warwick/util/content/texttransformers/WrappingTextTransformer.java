package uk.ac.warwick.util.content.texttransformers;


/**
 * Abstract implementation of TextTransformer which takes does some
 * pre-transformation, then the delegate transformation, then some
 * post-transformation
 *  
 * @author cusebr
 */
public abstract class WrappingTextTransformer implements TextTransformer {

    private TextTransformer transformer;
    
    public WrappingTextTransformer(final TextTransformer delegate) {
        transformer = delegate;
    }
    
    public abstract String preTransform(final String text);
    public abstract String postTransform(final String text);
    
    public final String transform(final String inputText) {
        String text = inputText;
        text = preTransform(text);
        text = transformer.transform(text);
        text = postTransform(text);
        return text;
    }

}
