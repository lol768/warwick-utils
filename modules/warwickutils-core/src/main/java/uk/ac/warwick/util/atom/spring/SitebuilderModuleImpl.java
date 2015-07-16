package uk.ac.warwick.util.atom.spring;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.sun.syndication.feed.module.ModuleImpl;

public final class SitebuilderModuleImpl extends ModuleImpl implements SitebuilderModule {
    private static final long serialVersionUID = -2113080234679423174L;
    
    private Boolean allowSearchEngines;
    private Boolean showInLocalNavigation;
    private String pageName;
    private Boolean deleted;
    private Boolean spanRhs;
    private String head;
    private String lastUpdateComment;
    private String description;
    private String keywords;
    private Boolean includeLegacyJavascript;
    private Integer pageOrder;
    private String linkTitle;
    private Boolean commentable;
    private Boolean commentVisibleToCommentersOnly;
    private String title;
    private String shortTitle;
    private String layout;
    
    public SitebuilderModuleImpl() {
        super(SitebuilderModule.class, MODULE_URI);
    }
    
    public Class<SitebuilderModule> getInterface() {
        return SitebuilderModule.class;
    }
    
    public void copyFrom(Object obj) {
        BeanWrapper other = new BeanWrapperImpl(obj);
        BeanWrapper wrapper = new BeanWrapperImpl(this);
        
        for (Property prop : Property.values()) {
            wrapper.setPropertyValue(prop.name(), other.getPropertyType(prop.name()));
        }
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

    public String getLastUpdateComment() {
        return lastUpdateComment;
    }

    public void setLastUpdateComment(String comment) {
        this.lastUpdateComment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Boolean getIncludeLegacyJavascript() {
        return includeLegacyJavascript;
    }

    public void setIncludeLegacyJavascript(Boolean includeLegacyJavascript) {
        this.includeLegacyJavascript = includeLegacyJavascript;
    }

    public Integer getPageOrder() {
        return pageOrder;
    }

    public void setPageOrder(Integer pageOrder) {
        this.pageOrder = pageOrder;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public Boolean getCommentable() {
        return commentable;
    }

    public void setCommentable(Boolean commentable) {
        this.commentable = commentable;
    }

    public Boolean getCommentVisibleToCommentersOnly() {
        return commentVisibleToCommentersOnly;
    }

    public void setCommentVisibleToCommentersOnly(Boolean commentVisibleToCommentersOnly) {
        this.commentVisibleToCommentersOnly = commentVisibleToCommentersOnly;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }    

}
