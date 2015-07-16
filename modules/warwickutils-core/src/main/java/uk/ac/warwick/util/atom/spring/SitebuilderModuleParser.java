package uk.ac.warwick.util.atom.spring;

import java.beans.PropertyEditor;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import uk.ac.warwick.util.atom.spring.SitebuilderModule.Property;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;

/**
 * ModuleParser is for grabbing the Sitebuilder elements while reading a 
 * feed, and returning them as a SitebuilderModule. 
 */
public final class SitebuilderModuleParser implements ModuleParser {

    private static final Namespace SB_NS = SitebuilderModule.NAMESPACE;
    
    public String getNamespaceUri() {
        return SitebuilderModule.MODULE_URI;
    }

    public Module parse(Element element) {
        SitebuilderModule module = new SitebuilderModuleImpl();
        BeanWrapper wrapper = new BeanWrapperImpl(module);
        boolean elementsFound = false;
        
        for (Property prop : SitebuilderModule.Property.values()) {
            Element el = element.getChild(prop.getElement(), SB_NS);
            if (el != null) {
                elementsFound = true;
                
                PropertyEditor editor = prop.newPropertyEditor();
                
                editor.setAsText(el.getValue());                
                wrapper.setPropertyValue(prop.name(), editor.getValue());
            }
        }
        
        return (elementsFound) ? module : null;
    }

}
