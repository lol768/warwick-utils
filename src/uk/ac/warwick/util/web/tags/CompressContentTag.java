package uk.ac.warwick.util.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class CompressContentTag extends BodyTagSupport {
    
    private static final long serialVersionUID = -8639817913739561015L;

    public final int doEndTag() throws JspException {

        String body = getBodyContent().getString().trim();
        if (body != null && !body.equals("")) {
            StringBuilder sb = new StringBuilder();
            String[] contents = body.replaceAll("\t|\r","").split("\n+");
            
            for (String line : contents) {
                line = line.trim();
                if (!line.equals("")) {
                    sb.append(line);
                }
            }
            
            String results = sb.toString();
            results = results.replaceAll("\\s{2,}", " ");

            try {
                pageContext.getOut().write(results);
            } catch (IOException e) {
                throw new JspTagException("Error:" + e.toString());
            }
        }

        return EVAL_PAGE;

    }

}
