package uk.ac.warwick.util.atom;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.util.StringUtils;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;

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
        SitebuilderModule sbm = (SitebuilderModule) module;
        
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
 
        if (StringUtils.hasText(sbm.getHead())) {
            element.addContent(element(SitebuilderModule.ELEMENT_HEAD, sbm.getHead()));
        }
        
        if (StringUtils.hasText(sbm.getPageName())) {
            element.addContent(element(SitebuilderModule.ELEMENT_NAME, sbm.getPageName()));
        }
        
        if (sbm.getAllowSearchEngines() != null) {
            element.addContent(element(SitebuilderModule.ELEMENT_SEARCHABLE, 
                    sbm.getAllowSearchEngines().toString()));
        }
        
        if (sbm.getShowInLocalNavigation() != null) {
            element.addContent(element(SitebuilderModule.ELEMENT_VISIBLE, 
                    sbm.getShowInLocalNavigation().toString()));
        }
        
        if (sbm.getDeleted() != null) {
            element.addContent(element(SitebuilderModule.ELEMENT_DELETED,
                    sbm.getDeleted().toString()));
        }
        
        if (sbm.getSpanRhs() != null) {
            element.addContent(element(SitebuilderModule.ELEMENT_SPAN_RHS,
                    sbm.getSpanRhs().toString()));
        }
        
        // edit-comment not present because it's only parsed as input. 
        
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
