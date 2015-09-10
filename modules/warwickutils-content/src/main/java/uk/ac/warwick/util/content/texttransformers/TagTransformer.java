package uk.ac.warwick.util.content.texttransformers;

import java.util.regex.Pattern;

/** 
 * A TagTransformer is constructed for a given tag name, and then
 * when transform() is called, it will pass any found tags to the
 * callback and replace it with whatever the callback returns. The
 * callback may choose to return the original string in which case
 * the tag is unchanged.
 * 
 * NOTE: This has been changed so that it captures the contents of the
 * tag, up to and including the end tag. HOWEVER it doesn't do depth
 * counting so tags that can nest will break with this simple pattern
 * matcher (it will match the wrong closing tag). 
 * 
 * This is only suitable for tags such as <a>, which can't nest.
 */
public final class TagTransformer extends TextPatternTransformer {
    private final Pattern pattern;
    private final Callback callback;
    private final boolean generatesHead;
    
    public TagTransformer(final String tagName, final Callback callback, final boolean generatesHead) {
        this(tagName,"<", ">", callback, generatesHead);
    }
    
    public TagTransformer(final String tagName, final String openSymbol, final String closeSymbol, final Callback callback, final boolean generatesHead) {
        String lt = openSymbol;
        String gt = closeSymbol;
        this.pattern = Pattern.compile(lt+tagName+" ([^"+gt+lt+"]+)"+gt+".+?"+lt+"/"+tagName+gt, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
        this.callback = callback;
        this.generatesHead = generatesHead;
    }

    @Override
    protected Pattern getPattern() {
        return pattern;
    }

    @Override
    protected Callback getCallback() {
        return callback;
    }

    @Override
    protected boolean isGeneratesHead() {
        return generatesHead;
    }
}
