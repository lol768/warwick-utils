package uk.ac.warwick.util.atom.spring;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.jdom.Namespace;

import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import uk.ac.warwick.util.collections.LazyList.Factory;
import uk.ac.warwick.util.core.StringUtils;

import com.google.common.collect.ImmutableSet;
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
    
    Factory<PropertyEditor> INTEGER_PROPERTY_EDITOR_FACTORY = new Factory<PropertyEditor>() {
        public PropertyEditor create() {
            return new PropertyEditorSupport() {
                @Override
                public void setAsText(String text) throws IllegalArgumentException {
                    setValue(Integer.valueOf(text));
                }
                
                @Override
                public String getJavaInitializationString() {
                    return ("" + getValue());
                }
            };
        }
    };
    
    Factory<CustomBooleanEditor> BOOLEAN_PROPERTY_EDITOR_FACTORY = new Factory<CustomBooleanEditor>() {
        public CustomBooleanEditor create() {
            return new CustomBooleanEditor(false);
        }
    };
    
    Factory<PropertyEditor> STRING_PROPERTY_EDITOR_FACTORY = new Factory<PropertyEditor>() {
        public PropertyEditor create() {
            return new PropertyEditorSupport() {
                @Override
                public void setAsText(String text) throws IllegalArgumentException {
                    String value = StringUtils.hasText(text) ? text.trim() : "";
                    
                    setValue(StringUtils.htmlEscapeHighCharacters(value));
                }
                
                @Override
                public String getAsText() {
                    return StringUtils.nullGuard(getValue() == null ? "" : getValue().toString());
                }
            };
        }
    };
    
    enum PropertyFlag {
        ReadOnly,
        EditSpecific,
        HtmlSpecific
    }
    
    public enum Property {
        /* Read only properties */
        pageName("page-name", PropertyFlag.ReadOnly),
        head("head", PropertyFlag.ReadOnly, PropertyFlag.HtmlSpecific),
        
        allowSearchEngines("searchable", BOOLEAN_PROPERTY_EDITOR_FACTORY),
        showInLocalNavigation("visible", BOOLEAN_PROPERTY_EDITOR_FACTORY),
        deleted("deleted", BOOLEAN_PROPERTY_EDITOR_FACTORY),
        description("description"),
        keywords("keywords"),
        pageOrder("page-order", INTEGER_PROPERTY_EDITOR_FACTORY),
        linkTitle("link-caption"),
        commentable("commentable", BOOLEAN_PROPERTY_EDITOR_FACTORY),
        commentVisibleToCommentersOnly("comments-visible-to-commenters-only", BOOLEAN_PROPERTY_EDITOR_FACTORY),
        
        /* Edit specific */
        lastUpdateComment("edit-comment", PropertyFlag.EditSpecific, PropertyFlag.HtmlSpecific),
        
        /* HTML specific */
        spanRhs("span-rhs", BOOLEAN_PROPERTY_EDITOR_FACTORY, PropertyFlag.HtmlSpecific),
        includeLegacyJavascript("include-legacy-js", BOOLEAN_PROPERTY_EDITOR_FACTORY, PropertyFlag.HtmlSpecific),
        title("page-heading", PropertyFlag.HtmlSpecific),
        shortTitle("title-bar-caption", PropertyFlag.HtmlSpecific),
        layout("layout", PropertyFlag.HtmlSpecific);
        
        private final String element;
        
        private final Factory<? extends PropertyEditor> factory;
        
        private final ImmutableSet<PropertyFlag> flags;
        
        Property(String e, PropertyFlag... theFlags) {
            this(e, STRING_PROPERTY_EDITOR_FACTORY, theFlags);
        }
        
        Property(String e, Factory<? extends PropertyEditor> editor, PropertyFlag... theFlags) {
            this.element = e;
            this.factory = editor;
            this.flags = ImmutableSet.copyOf(theFlags);
        }

        public String getElement() {
            return element;
        }

        public PropertyEditor newPropertyEditor() {
            return factory.create();
        }

        public boolean isEditSpecific() {
            return flags.contains(PropertyFlag.EditSpecific);
        }

        public boolean isHtmlSpecific() {
            return flags.contains(PropertyFlag.HtmlSpecific);
        }

        public boolean isReadOnly() {
            return flags.contains(PropertyFlag.ReadOnly);
        }
    }

    String MODULE_URI = "http://go.warwick.ac.uk/elab-schemas/atom";
    
    Namespace NAMESPACE = Namespace.getNamespace("sitebuilder", SitebuilderModule.MODULE_URI);
    Namespace ATOM_NAMESPACE = Namespace.getNamespace("http://www.w3.org/2005/Atom");
    Namespace ATOMPUB_NAMESPACE = Namespace.getNamespace("http://purl.org/atom/app#");

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
    
    String getLastUpdateComment();
    void setLastUpdateComment(String comment);
    
    String getDescription();
    void setDescription(String description);
    
    String getKeywords();
    void setKeywords(String keywords);
    
    Boolean getIncludeLegacyJavascript();
    void setIncludeLegacyJavascript(Boolean includeLegacyJavascript);

    Integer getPageOrder();
    void setPageOrder(Integer pageOrder);

    String getLinkTitle();
    void setLinkTitle(String linkTitle);

    Boolean getCommentable();
    void setCommentable(Boolean commentable);

    Boolean getCommentVisibleToCommentersOnly();
    void setCommentVisibleToCommentersOnly(Boolean commentVisibleToCommentersOnly);

    String getTitle();
    void setTitle(String title);

    String getShortTitle();
    void setShortTitle(String shortTitle);

    String getLayout();
    void setLayout(String layout);
}