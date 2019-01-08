package uk.ac.warwick.util.atom;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.ModuleImpl;

import java.util.Date;
import java.util.List;

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
    
    public void copyFrom(CopyFrom obj) {
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
