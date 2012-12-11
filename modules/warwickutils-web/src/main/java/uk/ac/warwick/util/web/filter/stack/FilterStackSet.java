package uk.ac.warwick.util.web.filter.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.Filter;

import org.apache.commons.lang3.builder.ToStringBuilder;

import uk.ac.warwick.util.collections.google.BasePredicate;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
    private final String name;
    
    private FilterMappingParser filterMappingParser;
    
    public FilterStackSet(List<Filter> tfilters, Collection<String> tincludedUrlPatterns) {
        this(tfilters, tincludedUrlPatterns, new ArrayList<String>());
    }
    
    public FilterStackSet(List<Filter> tfilters, Collection<String> tincludedUrlPatterns, Collection<String> texcludedUrlPatterns) {
    	this(tfilters, tincludedUrlPatterns, texcludedUrlPatterns, null);
    }
    
    public FilterStackSet(List<Filter> tfilters, Collection<String> tincludedUrlPatterns, Collection<String> texcludedUrlPatterns, String tname) {
        this.filters = ImmutableList.copyOf(tfilters);
        this.compositeFilter = new CompositeFilter(filters);
        this.includedUrlPatterns = ImmutableList.copyOf(tincludedUrlPatterns);
        this.excludedUrlPatterns = ImmutableList.copyOf(texcludedUrlPatterns);
        this.name = tname;
    }

    public List<Filter> getFilters() {
        return filters;
    }
    
    public String getName() {
		return name;
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
    
    public static FilterStackSet merge(FilterStackSet first, List<FilterStackSet> others) {
    	if (others.isEmpty()) {
    		return first;
    	} else if (others.size() == 1) {
    		return mergeTwo(first, others.get(0));
    	} else {
    		return merge(mergeTwo(first, others.get(0)), others.subList(1, others.size()));
    	}
    }
    
    private static FilterStackSet mergeTwo(FilterStackSet first, FilterStackSet second) {
    	return new FilterStackSet(
			combineNoDuplicates(first.filters, second.filters), 
			combineNoDuplicates(first.includedUrlPatterns, second.includedUrlPatterns), 
			combineNoDuplicates(first.excludedUrlPatterns, second.excludedUrlPatterns), 
			first.name
		);
    }
    
    private static <T> List<T> combineNoDuplicates(Collection<T> first, Collection<T> second) {
    	List<T> all = Lists.newArrayListWithCapacity(first.size() + second.size());
    	all.addAll(first);
    	
    	for (T value : second) {
    		addUnique(all, value);
    	}
    	
    	return all;
    }
    
    private static <T> boolean addUnique(List<T> list, T value) {
    	if (!list.contains(value)) return list.add(value);
    	else return false;
    }

	@Override
	public String toString() {
		return 
			new ToStringBuilder(this)
			.append("filters", filters)
			.append("includedUrls", includedUrlPatterns)
			.append("excludedUrls", excludedUrlPatterns)
			.append("name", name)
			.build();
	}
}
