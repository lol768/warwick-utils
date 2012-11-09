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
public final class TextileTag extends BodyTagSupport {

	private String stripAllHtmlBeforeConvert;

	private String stripAllHtmlAfterConvert;

	private String disallowTags;

	private String onlyAllowTags;
	
	private String correctHtml;
	
	private String lite;

	public int doEndTag() throws JspException {

		BodyContent body = getBodyContent();
		if (body != null) {
			String textileText = body.getString();
			TextileString textileString = new TextileString(textileText);
			textileString.setStripAllHtmlAfterConvert(new Boolean(getStripAllHtmlAfterConvert()).booleanValue());
			textileString.setStripAllHtmlBeforeConvert(new Boolean(getStripAllHtmlBeforeConvert()).booleanValue());
			textileString.setDisallowTags(getDisallowTags());
			textileString.setOnlyAllowTags(getOnlyAllowTags());
			textileString.setCorrectHtml(new Boolean(getCorrectHtml()).booleanValue());
			textileString.setLite(new Boolean(getLite()).booleanValue());
			String htmlText = textileString.getHtml();
			try {
				pageContext.getOut().write(htmlText);
			} catch (IOException e) {
				throw new JspTagException("Error:" + e.toString());
			}
		}

		return EVAL_PAGE;

	}

	public String getDisallowTags() {
		return disallowTags;
	}
	public void setDisallowTags(String disallowTags) {
		this.disallowTags = disallowTags;
	}
	public String getOnlyAllowTags() {
		return onlyAllowTags;
	}
	public void setOnlyAllowTags(String onlyAllowTags) {
		this.onlyAllowTags = onlyAllowTags;
	}
	public String getStripAllHtmlAfterConvert() {
		return stripAllHtmlAfterConvert;
	}
	public void setStripAllHtmlAfterConvert(String stripAllHtmlAfterConvert) {
		this.stripAllHtmlAfterConvert = stripAllHtmlAfterConvert;
	}
	public String getStripAllHtmlBeforeConvert() {
		return stripAllHtmlBeforeConvert;
	}
	public void setStripAllHtmlBeforeConvert(String stripAllHtmlBeforeConvert) {
		this.stripAllHtmlBeforeConvert = stripAllHtmlBeforeConvert;
	}
	public String getCorrectHtml() {
		return correctHtml;
	}
	public void setCorrectHtml(String correctHtml) {
		this.correctHtml = correctHtml;
	}
	public String getLite() {
		return lite;
	}
	public void setLite(String lite) {
		this.lite = lite;
	}
}