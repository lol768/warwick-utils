/*
 * Created on 11-Mar-2004
 *
 */
package uk.ac.warwick.util.content.textile2;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * @author Kieran Shaw
 *  
 */
@SuppressWarnings("serial")
public class EscapeBracketEntitiesTag extends BodyTagSupport {

	public final int doEndTag() throws JspException {

		BodyContent body = getBodyContent();
		if (body != null) {
			String converted = body.getString();
			converted = converted.replaceAll("&lt;","&amp;lt;");
			converted = converted.replaceAll("&gt;","&amp;gt;");
			try {
				pageContext.getOut().write(converted);
			} catch (IOException e) {
				throw new JspTagException("Error:" + e.toString());
			}
		}

		return EVAL_PAGE;

	}
}