package uk.ac.warwick.util.atom;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;
import com.sun.syndication.io.impl.DateParser;
import com.sun.syndication.io.impl.NumberParser;

/**
 * ModuleParser is for grabbing the Sitebuilder elements while reading a feed,
 * and returning them as a AtomModule.
 */
public final class AtomModuleParser implements ModuleParser {

    public String getNamespaceUri() {
        return AtomModule.MODULE_URI;
    }

    public Module parse(Element element) {
        AtomModule module = new AtomModuleImpl();
        boolean elementsFound = false;

        @SuppressWarnings("unchecked")
        List<Element> linkElements = element.getChildren(AtomModule.ELEMENT_LINK, AtomModule.NAMESPACE);
        if (!linkElements.isEmpty()) {
            elementsFound = true;
            
            List<Link> links = Lists.newArrayList();
            
            for (Element linkElement: linkElements) {
                links.add(parseLink(linkElement));
            }
            
            module.setLinks(links);
        }

        Element published = element.getChild(AtomModule.ELEMENT_PUBLISHED, AtomModule.NAMESPACE);
        if (published != null) {
            elementsFound = true;
            module.setPublishedDate(DateParser.parseDate(published.getText()));
        }

        Element updated = element.getChild(AtomModule.ELEMENT_UPDATED, AtomModule.NAMESPACE);
        if (updated != null) {
            elementsFound = true;
            module.setUpdatedDate(DateParser.parseDate(updated.getText()));
        }

        return (elementsFound) ? module : null;
    }

    private Link parseLink(Element eLink) {
        Link link = new Link();
        
        String att = getAttributeValue(eLink, "rel");
        if (att != null) {
            link.setRel(att);
        }
        
        att = getAttributeValue(eLink, "type");
        if (att != null) {
            link.setType(att);
        }
        
        att = getAttributeValue(eLink, "href");
        if (att != null) {
            link.setHref(att);
        }
        
        att = getAttributeValue(eLink, "title");
        if (att != null) {
            link.setTitle(att);
        }
        
        att = getAttributeValue(eLink, "hreflang");
        if (att != null) {
            link.setHreflang(att);
        }
        
        att = getAttributeValue(eLink, "length");
        if (att != null) {
            Long val = NumberParser.parseLong(att);
            if (val != null) {
                link.setLength(val.longValue());
            }
        }
        
        return link;
    }

    private String getAttributeValue(Element e, String attributeName) {
        Attribute attr = getAttribute(e, attributeName);
        return (attr != null) ? attr.getValue() : null;
    }

    private Attribute getAttribute(Element e, String attributeName) {
        Attribute attribute = e.getAttribute(attributeName);
        if (attribute == null) {
            attribute = e.getAttribute(attributeName, AtomModule.NAMESPACE);
        }
        return attribute;
    }

}
