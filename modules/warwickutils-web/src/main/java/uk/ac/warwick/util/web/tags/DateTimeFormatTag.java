package uk.ac.warwick.util.web.tags;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.util.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;

/**
 * Simple tag to reproduce a subset of behaviour of {@see FormatDateTag} so we
 * can format JSR310's {@link Temporal} objects in JSPs.
 * 
 * @author Mat
 */
public final class DateTimeFormatTag extends TagSupport implements TemplateMethodModelEx {

    private static final long serialVersionUID = 4876809538088164519L;

    private Temporal value;

    private String pattern;

    @Override
    public int doEndTag() throws JspException {
        String formatted = getFormattedDate();

        try {
            pageContext.getOut().print(formatted);
        } catch (IOException ioe) {
            throw new JspTagException(ioe.toString());
        }

        return EVAL_PAGE;
    }
    
    @SuppressWarnings("unchecked")
	public Object exec(List arguments) throws TemplateModelException {
    	if (arguments.size() != 2) {
    		throw new TemplateModelException(new IllegalArgumentException("Invalid number of arguments - should pass pattern, dateTime"));
    	}
    	
    	if (!SimpleScalar.class.isAssignableFrom(arguments.get(0).getClass())) {
    		throw new TemplateModelException(new IllegalArgumentException("Invalid argument - first argument should be a String pattern"));
    	}
    	
    	String p = ((SimpleScalar)arguments.get(0)).getAsString();
    	
    	if (!StringModel.class.isAssignableFrom(arguments.get(1).getClass()) 
    		|| !Temporal.class.isAssignableFrom(((StringModel)arguments.get(1)).getWrappedObject().getClass())) {
    		throw new TemplateModelException(new IllegalArgumentException("Invalid argument - second argument should be a Temporal"));
    	}

        Temporal v = (Temporal)((StringModel)arguments.get(1)).getWrappedObject();
    	
		return getFormattedDate(p, v);
	}
    
    public String getFormattedDate() {
    	return getFormattedDate(pattern, value);
    }
    
    public String getFormattedDate(String p, Temporal v) {
    	if (!StringUtils.hasText(p) || v == null) {
    		throw new IllegalArgumentException();
    	}
    	
    	return DateTimeFormatter.ofPattern(p).format(v);
    }

    public void setValue(Temporal value) {
        this.value = value;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}
