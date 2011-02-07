package uk.ac.warwick.util.content.texttransformers;

import java.util.Map;
import java.util.regex.Matcher;

import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.TextPatternTransformer.Callback;

/**
 * Magic tag support for page summary. Very simple; just wraps the content in a div.
 * 
 * @author Mat Mannion
 */
public final class PageSummaryTagTextTransformer extends AbstractSquareTagTransformer {
    
    public static final String SUMMARY_DIV_ID = "sbr_pagesummary";

    private static final String[] ALLOWED_PARAMETERS = new String[] { "visible" };

//    private static final Logger LOGGER = Logger.getLogger(PageSummaryTagTextTransformer.class);

    public PageSummaryTagTextTransformer() {
        super("page-summary",true,true,true);
    }

    @Override
    protected Callback getCallback() {
        return new TextPatternTransformer.Callback() {

            public String transform(final String input, final MutableContent mc) {
                Matcher matcher = getTagPattern().matcher(input);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Failed to match summary tag, but shouldn't be here if it didn't");
                }
                Map<String, Object> parameters = getParameters(matcher);
                String contents = getContents(matcher);
                
                boolean visible = true;

                if (parameters.containsKey("visible")) {
                    visible = !((String)parameters.get("visible")).equalsIgnoreCase("false");
                }

                StringBuilder sb = new StringBuilder();
                
                sb.append("<div id=\"");
                sb.append(SUMMARY_DIV_ID);
                sb.append("\"");
                
                if (!visible) {
                    sb.append(" style=\"display:none;\"");
                }
                
                sb.append(">");
                sb.append(contents);
                sb.append("</div>");

                return sb.toString();
            }

        };
    }

    @Override
    protected String[] getAllowedParameters() {
        return ALLOWED_PARAMETERS;
    }

}
