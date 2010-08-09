package uk.ac.warwick.util.atom;

import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;

/**
 * This module represents our extensions to the ATOM feed to
 * describe properties about a page.
 * 
 * If you add a new property here, you will need to amend
 * the Generator and Parser classes to handle converting the
 * property to and from the XML element. This generally involves
 * getting/creating an &lt;element&gt; with a particular name.
 */
public interface SitebuilderModule extends Module {

    String MODULE_URI = "http://go.warwick.ac.uk/elab-schemas/atom";
    
    Namespace NAMESPACE = Namespace.getNamespace("sitebuilder", SitebuilderModule.MODULE_URI);
    Namespace ATOM_NAMESPACE = Namespace.getNamespace("http://www.w3.org/2005/Atom");
    Namespace ATOMPUB_NAMESPACE = Namespace.getNamespace("http://purl.org/atom/app#");
    
    String ELEMENT_SEARCHABLE = "searchable";
    String ELEMENT_VISIBLE = "visible";
    String ELEMENT_NAME = "page-name";
    String ELEMENT_HEAD = "head";
    String ELEMENT_DELETED = "deleted";
    String ELEMENT_SPAN_RHS = "span-rhs";

    String getPageName();
    void setPageName(String name);
    
    Boolean getAllowSearchEngines();
    void setAllowSearchEngines(final Boolean allowSearchEngines);
    
    Boolean getShowInLocalNavigation();
    void setShowInLocalNavigation(final Boolean showInLocalNavigation);
    
    Boolean getDeleted();
    void setDeleted(final Boolean deleted);
    
    Boolean getSpanRhs();
    void setSpanRhs(final Boolean span);
    
    String getHead();
    void setHead(String head);
}