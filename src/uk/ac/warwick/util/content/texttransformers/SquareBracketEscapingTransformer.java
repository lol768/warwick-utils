package uk.ac.warwick.util.content.texttransformers;

import uk.ac.warwick.util.content.MutableContent;


/**
 * @todo This basically disables any of Textile's uses for square brackets. This needs
 * to do some more complex parsing so that it only escapes brackets related to our
 * particular endeavours. eg [media.*?] and [/media] 
 * 
 * @author cusebr
 */
public final class SquareBracketEscapingTransformer extends WrappingTextTransformer {

    private static final String OPEN_BRACKET = "&#91;";
    private static final String CLOSE_BRACKET = "&#93;";
    
    private static final String[] TAGS_TO_ESCAPE = new String[] { "media", "latex", "livesearch" };
    
    public SquareBracketEscapingTransformer(final TextTransformer delegate) {
        super(delegate);
    }

    @Override
    public MutableContent preTransform(final MutableContent mc) {
        String text = mc.getContent();
        for (String tag : TAGS_TO_ESCAPE) {
            text = text.replaceAll("\\["+tag+"(.*?)\\]", OPEN_BRACKET +tag+"$1" + CLOSE_BRACKET)
                   .replaceAll("\\[/"+tag+"\\]", OPEN_BRACKET + "/"+tag + CLOSE_BRACKET);
        }
        
        mc.setContent(text);
        return mc;
    }

    @Override
    public MutableContent postTransform(final MutableContent mc) {
        String text = mc.getContent();
        text = text.replaceAll(OPEN_BRACKET, "[").replaceAll(CLOSE_BRACKET, "]");
        mc.setContent(text);
        return mc;
    }

}
