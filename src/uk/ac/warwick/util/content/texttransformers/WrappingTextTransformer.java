package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;


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
    
    public abstract MutableContent preTransform(final MutableContent text);
    public abstract MutableContent postTransform(final MutableContent text);
    
    public final MutableContent apply(MutableContent mc) {
        mc = preTransform(mc);
        mc = transformer.apply(mc);
        mc = postTransform(mc);
        return mc;
    }

}
