package uk.ac.warwick.util.atom;

import java.util.Date;
import java.util.List;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.module.ModuleImpl;

public final class AtomModuleImpl extends ModuleImpl implements AtomModule {
    private static final long serialVersionUID = -2113080234679423174L;
    
    // Don't use DateTime, not cloneable
    private List<Link> links;
    private Date publishedDate;
    private Date updatedDate;
    
    public AtomModuleImpl() {
        super(AtomModule.class, AtomModule.MODULE_URI);
    }
    
    public Class<AtomModule> getInterface() {
        return AtomModule.class;
    }
    
    public void copyFrom(Object obj) {
        AtomModule module = (AtomModule) obj;
        links = module.getLinks();
        publishedDate = module.getPublishedDate();
        updatedDate = module.getUpdatedDate();
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

}
