package uk.ac.warwick.util.web.filter.stack.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public final class FilterStackNamespaceHandler extends NamespaceHandlerSupport {

    public static final String NS = "http://go.warwick.ac.uk/elab-schemas/filterstack";
    
    public void init() {
        registerBeanDefinitionParser("filter-stack", new FilterStackDefinitionParser());
    }

}
