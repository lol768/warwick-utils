package uk.ac.warwick.util.atom;

import com.sun.syndication.feed.module.ModuleImpl;

public final class SitebuilderModuleImpl extends ModuleImpl implements SitebuilderModule {
    private static final long serialVersionUID = -2113080234679423174L;
    
    private Boolean allowSearchEngines;
    private Boolean showInLocalNavigation;
    private String pageName;
    private Boolean deleted;
    private Boolean spanRhs;
    private String head;
    private String comment;
    
    public SitebuilderModuleImpl() {
        super(SitebuilderModule.class, SitebuilderModule.MODULE_URI);
    }
    
    public Class<SitebuilderModule> getInterface() {
        return SitebuilderModule.class;
    }
    
    public void copyFrom(Object obj) {
        SitebuilderModule module = (SitebuilderModule) obj;
        allowSearchEngines = module.getAllowSearchEngines();
        showInLocalNavigation = module.getShowInLocalNavigation();
        pageName = module.getPageName();
        deleted = module.getDeleted();
        spanRhs = module.getSpanRhs();
        head = module.getHead();
        comment = module.getComment();
    }
    
    public Boolean getAllowSearchEngines() {
        return allowSearchEngines;
    }

    public void setAllowSearchEngines(Boolean allowSearchEngines) {
        this.allowSearchEngines = allowSearchEngines;
    }

    public Boolean getShowInLocalNavigation() {
        return showInLocalNavigation;
    }

    public void setShowInLocalNavigation(Boolean showInLocalNavigation) {
        this.showInLocalNavigation = showInLocalNavigation;
    }
    

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getSpanRhs() {
        return spanRhs;
    }

    public void setSpanRhs(final Boolean spanRhs) {
        this.spanRhs = spanRhs;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    

}
