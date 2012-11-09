package uk.ac.warwick.util.web.tags;

import java.util.Collection;
import java.util.Map;

import javax.servlet.jsp.tagext.TagSupport;

import uk.ac.warwick.util.collections.CollectionUtils;

/**
 * <p>Groups the specified collection by the value of the specified parameter.</p>
 * @author yatesco
 */
public final class CollectionGrouperTag extends TagSupport {
    private static final long serialVersionUID = -7357664897018370348L;
    private static final String DEFAULT_MAP_ATTRIBUTE_NAME = "result";	// name of page attribute
    private Collection<?> objects;
    private String property;
    private String var = DEFAULT_MAP_ATTRIBUTE_NAME;

    @Override
	public int doStartTag() {
        Map<String, ?> map = CollectionUtils.groupByProperty(objects, property);
        pageContext.setAttribute(var, map);
        return EVAL_BODY_INCLUDE;
    }

    public void setObjects(final Collection<?> theObjects) {
        this.objects = theObjects;
    }

    public void setProperty(final String property) {
        this.property = property;
    }
    
    public void setVar(final String var) {
        this.var = var;
    }
}