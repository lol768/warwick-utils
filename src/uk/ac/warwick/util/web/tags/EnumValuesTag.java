package uk.ac.warwick.util.web.tags;

import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.google.common.collect.Lists;

public final class EnumValuesTag extends TagSupport {
    
    private static final long serialVersionUID = 6819886693312533932L;

    private String var;
    
    private String className;
    
    @Override
    public int doStartTag() throws JspException {
        List<?> values = getValues(className);
        
        pageContext.setAttribute(var, values);
        return EVAL_BODY_INCLUDE;
    }

    public static List<?> getValues(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (!clazz.isEnum()) {
                throw new IllegalStateException("Not an enum class: " + className);
            }
            
            final Method valuesMethod = clazz.getMethod("values");
            return Lists.newArrayList((Object[])valuesMethod.invoke(null));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid enum class: " + className, e);
        }
    }

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(var);
        return super.doEndTag();
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
