package uk.ac.warwick.util.atom;

import org.jdom.Element;
import org.jdom.Namespace;

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
        boolean elementsFound = false;
        
        Element searchable = element.getChild(SitebuilderModule.ELEMENT_SEARCHABLE, SB_NS);
        if (searchable != null) {
            elementsFound = true;
            module.setAllowSearchEngines(isTrue(searchable));
        }
        
        Element navvisible = element.getChild(SitebuilderModule.ELEMENT_VISIBLE, SB_NS);
        if (navvisible != null) {
            elementsFound = true;
            module.setShowInLocalNavigation(isTrue(navvisible));
        }
        
        Element pageName = element.getChild(SitebuilderModule.ELEMENT_NAME, SB_NS);
        if (pageName != null) {
            elementsFound = true;
            module.setPageName(pageName.getValue());
        }
        
        Element deleted = element.getChild(SitebuilderModule.ELEMENT_DELETED, SB_NS);
        if (deleted != null) {
            elementsFound = true;
            module.setDeleted(isTrue(deleted));
        }
        
        Element spanRhs = element.getChild(SitebuilderModule.ELEMENT_SPAN_RHS, SB_NS);
        if (spanRhs != null) {
            elementsFound = true;
            module.setSpanRhs(isTrue(spanRhs));
        }
        
        Element head = element.getChild(SitebuilderModule.ELEMENT_HEAD, SB_NS);
        if (head != null) {
            elementsFound = true;
            module.setHead(head.getValue());
        }
        
        return (elementsFound) ? module : null;
    }
    
    private Boolean isTrue(Element booleanElement) {
        return "true".equals(booleanElement.getValue());
    }

}
