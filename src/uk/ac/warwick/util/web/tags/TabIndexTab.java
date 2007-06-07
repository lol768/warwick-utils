package uk.ac.warwick.util.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

public final class TabIndexTab extends TagSupport {
    private static final long serialVersionUID = -690579672095213395L;
    private static final String TAB_INDEX_ATTRIBUTE = "tabIndex";
    private static final int FIRST_TABINDEX_VALUE = 1;
    
    public int doStartTag() throws JspException {        
        int tabIndex = getTabIndex();
        try {
            pageContext.getOut().print(tabIndex);
        } catch (IOException e) {
            throw new JspException(e);
        } finally {
            tabIndex++;
            setTabIndex(tabIndex);
        }  
        return SKIP_BODY;
    }


    private void setTabIndex(final int tabIndex) {
        pageContext.setAttribute(TAB_INDEX_ATTRIBUTE,tabIndex, PageContext.REQUEST_SCOPE);
    }

    private int getTabIndex() {
        if (pageContext.getAttribute(TAB_INDEX_ATTRIBUTE, PageContext.REQUEST_SCOPE) == null) {
            pageContext.setAttribute(TAB_INDEX_ATTRIBUTE, FIRST_TABINDEX_VALUE, PageContext.REQUEST_SCOPE);
        }
        return (Integer)pageContext.getAttribute(TAB_INDEX_ATTRIBUTE, PageContext.REQUEST_SCOPE);
    }
}
