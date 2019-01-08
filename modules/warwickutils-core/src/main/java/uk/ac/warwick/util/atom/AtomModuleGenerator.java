package uk.ac.warwick.util.atom;

import com.google.common.collect.ImmutableSet;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;
import com.rometools.rome.io.impl.DateParser;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Locale;
import java.util.Set;

public final class AtomModuleGenerator implements ModuleGenerator {

    private static final Set<Namespace> NAMESPACES = ImmutableSet.of(AtomModule.NAMESPACE);

    /**
     * Generate the additional elements. As we've currently
     * only assigned this class as an entry module, we assume Element
     * is the entry element. Otherwise we'd need to check whether
     * it was a feed or an entry.
     */
    public void generate(Module module, Element element) {
        AtomModule atomModule = (AtomModule) module;
        
        // Add the namespace declaration to the feed. Notice that
        // we're doing it on the entry though, because on some operations
        // we make a feed then strip out the single entry. If we just add
        // the namespace to the feed it gets lost.
        Element root = element;
        
        while (root.getParent()!=null && root.getParent() instanceof Element
                && !root.getName().equals("entry")) {
            root = (Element) element.getParent();
        }
        
        root.addNamespaceDeclaration(AtomModule.NAMESPACE);
 
        if (atomModule.getLinks() != null) {
            for (Link link : atomModule.getLinks()) {
                element.addContent(generateLinkElement(link));
            }
        }
        
        if (atomModule.getPublishedDate() != null) {
            element.addContent(element(AtomModule.ELEMENT_PUBLISHED, DateParser.formatW3CDateTime(atomModule.getPublishedDate(), Locale.getDefault())));
        }
        
        if (atomModule.getUpdatedDate() != null) {
            element.addContent(element(AtomModule.ELEMENT_UPDATED, DateParser.formatW3CDateTime(atomModule.getUpdatedDate(), Locale.getDefault())));
        }
    }

    private Element generateLinkElement(Link link) {
        Element linkElement = new Element(AtomModule.ELEMENT_LINK, AtomModule.NAMESPACE);

        if (link.getRel() != null) {
            Attribute relAttribute = new Attribute("rel", link.getRel());
            linkElement.setAttribute(relAttribute);
        }

        if (link.getType() != null) {
            Attribute typeAttribute = new Attribute("type", link.getType());
            linkElement.setAttribute(typeAttribute);
        }

        if (link.getHref() != null) {
            Attribute hrefAttribute = new Attribute("href", link.getHref());
            linkElement.setAttribute(hrefAttribute);
        }
        
        if (link.getHreflang() != null) {
            Attribute hreflangAttribute = new Attribute("hreflang", link.getHreflang());
            linkElement.setAttribute(hreflangAttribute);
        }
        if (link.getTitle() != null) {
            Attribute title = new Attribute("title", link.getTitle());
            linkElement.setAttribute(title);
        }
        if (link.getLength() != 0) {
            Attribute lenght = new Attribute("length", Long.toString(link.getLength()));
            linkElement.setAttribute(lenght);
        }
        return linkElement;
    }

    private Element element(String name, String value) {
        Element element = new Element(name, AtomModule.NAMESPACE);
        element.addContent(value);
        return element;
    }

    public String getNamespaceUri() {
        return AtomModule.MODULE_URI;
    }

    public Set<Namespace> getNamespaces() {
        return NAMESPACES;
    }

}
