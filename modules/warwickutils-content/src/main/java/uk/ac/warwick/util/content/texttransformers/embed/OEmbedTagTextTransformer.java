package uk.ac.warwick.util.content.texttransformers.embed;

import java.util.regex.Matcher;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.AbstractSquareTagTransformer;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;
import uk.ac.warwick.util.web.Uri;

public final class OEmbedTagTextTransformer extends
		AbstractSquareTagTransformer {
	
	static final String[] ALLOWED_PARAMETERS = new String[] {/* maxwidth, maxheight? */};
	
	private final OEmbed oembed;
	
	public OEmbedTagTextTransformer(OEmbed theOembed) {
		super("oembed", "embed");
		this.oembed = theOembed;
	}

	@Override
	protected boolean isTagGeneratesHead() {
		return false;
	}

	@Override
	protected String[] getAllowedParameters() {
		return ALLOWED_PARAMETERS;
	}

	@Override
	protected Callback getCallback() {
		return new TextPatternTransformer.Callback(){
            public String transform(final String input, final MutableContent mc) {
                Matcher matcher = getTagPattern().matcher(input);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Failed to match media tag, but shouldn't be here if it didn't");
                }
                //Map<String, Object> parameters = getParameters(matcher);
                String address = getContents(matcher);
                
                String result = input;
                
                try {
                	OEmbedResponse response = oembed.transformUrl(Uri.parse(address));
                	if (response != null) {
                		String renderedResponse = response.render();
                		if (renderedResponse != null) {
                			result = renderedResponse;
                		}
                	}
                } catch (OEmbedException e) {
                	// do nothing (for now)
                }
                
                return result;
            }
		};
	}

}
