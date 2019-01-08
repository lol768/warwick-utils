package uk.ac.warwick.util.atom;

import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import org.jdom2.Namespace;

import java.util.Date;
import java.util.List;

/**
 * A module for atom: elements in RSS feeds.
 */
public interface AtomModule extends Module {

    String MODULE_URI = "http://www.w3.org/2005/Atom";
    
    Namespace NAMESPACE = Namespace.getNamespace("atom", AtomModule.MODULE_URI);
    Namespace ATOM_NAMESPACE = Namespace.getNamespace(MODULE_URI);
    Namespace ATOMPUB_NAMESPACE = Namespace.getNamespace("http://purl.org/atom/app#");
    
    String ELEMENT_LINK = "link";
    String ELEMENT_PUBLISHED = "published";
    String ELEMENT_UPDATED = "updated";
    
    List<Link> getLinks();
    void setLinks(List<Link> links);

    Date getPublishedDate();
    void setPublishedDate(Date published);
    
    Date getUpdatedDate();
    void setUpdatedDate(Date updated);
    
}