package uk.ac.warwick.util.web.filter.stack.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.web.filter.stack.ConfigurableFilterStack;
import uk.ac.warwick.util.web.filter.stack.FilterStackSet;

public final class FilterStackDefinitionParser extends AbstractBeanDefinitionParser {
    
    private static final String EXCLUDED_URL_PATTERN_ELEMENT = "excluded-url-pattern";
    private static final String URL_PATTERN_ELEMENT = "url-pattern";
    private static final String FILTER_ELEMENT = "filter";
    private static final String MAPPING_ELEMENT = "mapping";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element rootElement, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.genericBeanDefinition(ConfigurableFilterStack.class);
        
        if (rootElement.hasAttribute("abstract") && "true".equals(rootElement.getAttribute("abstract"))) {
        	factory.setAbstract(true);
        }
        
        if (rootElement.hasAttribute("parent")) {
        	factory.setParentName(rootElement.getAttribute("parent"));
        }

        // A ManagedList supports containing BeanReferences and BeanDefinitions
        // as elements, which will get resolved properly.
        ManagedList sets = new ManagedList();
        sets.setMergeEnabled(parserContext.getDelegate().parseMergeAttribute(rootElement));
        for (Element element : getChildElementsByTagName(rootElement, MAPPING_ELEMENT)) {
            AbstractBeanDefinition stackSet = handleMappingElement(element, parserContext);
            sets.add(stackSet);
        }

        factory.addConstructorArgValue(sets);
        return factory.getBeanDefinition();
    }
    
    @SuppressWarnings("unchecked")
    private AbstractBeanDefinition handleMappingElement(Element mappingElement, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FilterStackSet.class);
 
        ManagedList filters = new ManagedList();
        for (Element element : getChildElementsByTagName(mappingElement, FILTER_ELEMENT)) {
            filters.add(new RuntimeBeanReference(getFilterName(element)));
        }
        
        List<String> includedPatterns = new ArrayList<String>();
        for (Element element : getChildElementsByTagName(mappingElement, URL_PATTERN_ELEMENT)) {
            includedPatterns.add(element.getTextContent());
        }
        
        List<String> excludedPatterns = new ArrayList<String>();
        for (Element element : getChildElementsByTagName(mappingElement, EXCLUDED_URL_PATTERN_ELEMENT)) {
            excludedPatterns.add(element.getTextContent());
        }
        
        String name = mappingElement.getAttribute("name");
        
        builder.addConstructorArgValue(filters);
        builder.addConstructorArgValue(includedPatterns);
        builder.addConstructorArgValue(excludedPatterns);
        builder.addConstructorArgValue(name);
        return builder.getBeanDefinition();
    }

    /**
     * Obtain the name of the referenced filter from the filter element.
     */
    private String getFilterName(Element filterElement) {
        String attribute = filterElement.getAttribute("ref");
        String name = null;
        if (StringUtils.hasText(attribute)) {
            name = attribute;
        } else {
            Element element = DomUtils.getChildElementByTagName(filterElement, "ref");
            String bean = element.getAttribute("bean");
            String local = element.getAttribute("local");
            if (StringUtils.hasText(bean)) {
                name = bean;
            } else if (StringUtils.hasText(local)) {
                name = local;
            }
        }
        return name;
    }
    
    @SuppressWarnings("unchecked")
    private List<Element> getChildElementsByTagName(Element parent, String name) {
        return(List<Element>)DomUtils.getChildElementsByTagName(parent, name);
    }

}
