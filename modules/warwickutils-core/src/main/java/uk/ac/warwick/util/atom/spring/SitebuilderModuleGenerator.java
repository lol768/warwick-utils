package uk.ac.warwick.util.atom.spring;

import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import uk.ac.warwick.util.atom.spring.SitebuilderModule.Property;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;
import uk.ac.warwick.util.core.StringUtils;

public final class SitebuilderModuleGenerator implements ModuleGenerator {

    private static final Set<Namespace> NAMESPACES;

    static {
        Set<Namespace> nss = new HashSet<Namespace>();
        nss.add(SitebuilderModule.NAMESPACE);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    /**
     * Generate the additional elements. As we've currently
     * only assigned this class as an entry module, we assume Element
     * is the entry element. Otherwise we'd need to check whether
     * it was a feed or an entry.
     */
    public void generate(Module module, Element element) {
        BeanWrapper wrapper = new BeanWrapperImpl(module);
        
        // Add the namespace declaration to the feed. Notice that
        // we're doing it on the entry though, because on some operations
        // we make a feed then strip out the single entry. If we just add
        // the namespace to the feed it gets lost.
        Element root = element;
        while (root.getParent()!=null && root.getParent() instanceof Element
                && !root.getName().equals("entry")) {
            root = (Element) element.getParent();
        }
        root.addNamespaceDeclaration(SitebuilderModule.NAMESPACE);
        
        for (Property prop : SitebuilderModule.Property.values()) {
            if (!prop.isEditSpecific()) {
                PropertyEditor editor = prop.newPropertyEditor();
                Object value = wrapper.getPropertyValue(prop.name()); 
                
                if (value != null) {
                    editor.setValue(value);
                    
                    String asText = editor.getAsText();
                    if (StringUtils.hasText(asText)) {
                        element.addContent(element(prop.getElement(), asText));
                    }
                }
            }
        }        
    }

    private Element element(String name, String value) {
        Element element = new Element(name, SitebuilderModule.NAMESPACE);
        element.addContent(value);
        return element;
    }

    public String getNamespaceUri() {
        return SitebuilderModule.MODULE_URI;
    }

    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

}
