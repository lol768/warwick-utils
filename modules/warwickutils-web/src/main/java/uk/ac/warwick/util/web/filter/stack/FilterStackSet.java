package uk.ac.warwick.util.web.filter.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.Filter;

import uk.ac.warwick.util.collections.google.BasePredicate;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * <p>
 * Represents a list of filters which should map to a list
 * of url patterns. If you have N filters and M url patterns,
 * normally in web.xml that would require N*M separate
 * filter-mapping entries.
 * <p>
 * Also specifies a list of URL patterns to <i>exclude</i> -
 * these override the regular patterns so if a URL matches an
 * include pattern and an exclude pattern, these filters won't
 * run.
 * <p>
 * This used to implement Filter so that they could be neatly
 * chained in a CompositeFilter, but this makes it difficult to
 * optimise. 
 */
public final class FilterStackSet {
    
    private final ImmutableList<Filter> filters;
    private final ImmutableCollection<String> includedUrlPatterns;
    private final ImmutableCollection<String> excludedUrlPatterns;
    private final CompositeFilter compositeFilter;
    
    private FilterMappingParser filterMappingParser;
    
    public FilterStackSet(List<Filter> tfilters, Collection<String> tincludedUrlPatterns, Collection<String> texcludedUrlPatterns) {
        this.filters = ImmutableList.copyOf(tfilters);
        this.compositeFilter = new CompositeFilter(filters);
        this.includedUrlPatterns = ImmutableList.copyOf(tincludedUrlPatterns);
        this.excludedUrlPatterns = ImmutableList.copyOf(texcludedUrlPatterns);
    }
    
    public FilterStackSet(List<Filter> tfilters, Collection<String> tincludedUrlPatterns) {
        this(tfilters, tincludedUrlPatterns, new ArrayList<String>());
    }

    public List<Filter> getFilters() {
        return filters;
    }
    
    public boolean isMatch(String urlPath) {
        BasePredicate<String> matcher = matcher(urlPath);
        return matcher.any(includedUrlPatterns) && !matcher.any(excludedUrlPatterns);
    }

    public void setFilterMappingParser(FilterMappingParser parser) {
        this.filterMappingParser = parser; 
    }

    public CompositeFilter getCompositeFilter() {
        return compositeFilter;
    }
    
    private BasePredicate<String> matcher(final String urlPath) {
        return new BasePredicate<String>() {
            @Override
            public boolean apply(String pattern) {
                return filterMappingParser.matches(urlPath, pattern);
            }
        };
    }
}
