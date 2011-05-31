package uk.ac.warwick.util.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Snips a long URL at each forward slash and puts a barely visible space before the next word, causing the browser to wrap to the next line if necessary.
 * 
 * @author Mat Mannion
 */
public final class WrappingUrlTag extends TagSupport {
    
    public static final String GLUE = "<wbr>";

    private static final long serialVersionUID = 8979598274671663491L;

    private String url;

    public WrappingUrlTag() {}

    public int doStartTag() throws JspException {
        String wrappableUrl = getWrappableUrl();

        try {
            pageContext.getOut().write(wrappableUrl);
        } catch (final IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    String getWrappableUrl() {
        StringBuilder sb = new StringBuilder();
        
        for (int i=0; i<url.length(); i++) {
            char c = url.charAt(i);
            sb.append(c);
            if ((c == '/') && (i != url.length()-1) && url.charAt(i+1) != '/') {
                sb.append(GLUE);
            }
        }
        
        return sb.toString();
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
