package uk.ac.warwick.util.web.filter.stack;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import uk.ac.warwick.util.collections.google.BasePredicate;
import uk.ac.warwick.util.core.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


/**
 * <p>
 * Work in progress. A superfilter which takes groups of FilterStackSets.
 * Each FilterStackSet maps a list of Filters to a collection of url patterns
 * which they map to.
 * <p>
 * It is designed as a replacement for putting your filters
 * in web.xml, as it is more flexible. Use by defining just this filter
 * in your web.xml, and keeping the rest of your filters as regular Spring beans.
 * It is okay to keep other filters in web.xml, but it's best not to use these
 * in {@link ConfigurableFilterStack} because having a filter in both places
 * may cause unexpected results (such as init() being called twice). 
 * <p>
 * FiltersStackSets are executed in the order provided, and Filters are executed
 * in the order they are provided within the sets, so when configured in Spring XML
 * the filters should be executed from top to bottom.
 * 
 * @author cusebr
 */
public final class ConfigurableFilterStack implements Filter, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableFilterStack.class);
    
    private final ImmutableList<FilterStackSet> filterSets;
    private final LoadingCache<String, CompositeFilter> cache =
        Caffeine.newBuilder()
            .maximumSize(10_000)
            .build(new FilterChainFactory());
    
    private FilterMappingParser parser;
    private boolean executeLifecycleEvents = true;
    
    public ConfigurableFilterStack(final List<FilterStackSet> filters) {
        this.filterSets = merge(filters);
    }
    
    /**
     * Combine any filters that have a name with any other definitions later on
     */
    public static ImmutableList<FilterStackSet> merge(List<FilterStackSet> filters) {
    	ImmutableList.Builder<FilterStackSet> builder = ImmutableList.builder();
    	
    	// Collect already handled names - this allows us to skip elements with the same name
    	Set<String> handledNames = Sets.newHashSet();
    	
    	// Create a copy of the list to do searches on
    	ImmutableList<FilterStackSet> copy = ImmutableList.copyOf(filters);
    	
    	for (FilterStackSet set : filters) {
    		String name = set.getName();
    		
    		if (!StringUtils.hasText(name)) {
    			builder.add(set);
    		} else if (handledNames.add(name)) {
    			builder.add(FilterStackSet.merge(set, withName(name, set).filteredCopy(copy)));
    		}
    	}
    	
    	return builder.build();
    }
    
    private static BasePredicate<FilterStackSet> withName(final String name, final FilterStackSet dupe) {
    	return new BasePredicate<FilterStackSet>() {
			@Override
			public boolean apply(FilterStackSet set) {
				return set != dupe && name.equals(set.getName());
			}
		};
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        // generate path - requestURI is /sitebuilder2/render/renderPage.htm so
        // get contextpath (/sitebuilder2) and trim it off the front to get the path for filtering
        String urlPath = request.getRequestURI().substring(request.getContextPath().length());
        CompositeFilter filter = cache.get(urlPath);
        filter.doFilter(req, res, chain);
    }

    public void init(final FilterConfig cfg) throws ServletException {
        if (!executeLifecycleEvents) return;
        LOGGER.info("Calling init() on inner filters");
        int filterNumber = 0;
        Set<Filter> inittedFilters = new HashSet<Filter>();
        for (FilterStackSet set : filterSets) {
            for (Filter filter : set.getFilters()) {
                if (inittedFilters.add(filter)) {
                    filterNumber++;
                    String name = "internalFilter" + filterNumber;
                    InnerFilterConfig config = new InnerFilterConfig(name, cfg.getServletContext());
                    filter.init(config);
                }
            }
        }
        inittedFilters.clear();
    }
    
    public static final class InnerFilterConfig implements FilterConfig {
        private final String filterName;
        private final ServletContext servletContext;
        
        public InnerFilterConfig(String name, ServletContext context) {
            this.filterName = name;
            this.servletContext = context;
        }

        public String getFilterName() {
            return filterName;
        }

        public String getInitParameter(String arg0) {
            return null;
        }

        public Enumeration<String> getInitParameterNames() {
            return new Vector<String>().elements();
        }

        public ServletContext getServletContext() {
            return servletContext;
        }
        
    }
    
    public void destroy() {
        if (!executeLifecycleEvents) return;
        LOGGER.info("Calling destroy() on inner filters");
        Set<Filter> destroyedFilters = new HashSet<Filter>();
        for (FilterStackSet set : filterSets) {
            for (Filter filter : set.getFilters()) {
                if (destroyedFilters.add(filter)) {
                    filter.destroy();
                }
            }
        }
        destroyedFilters.clear();
    }

    /**
     * @see #setExecuteLifecycleEvents(boolean)
     */
    public boolean isExecuteLifecycleEvents() {
        return executeLifecycleEvents;
    }

    /**
     * Sets whether this filter will delegate to the init() and destroy()
     * methods of the inner filters. Default is on. It will attempt to track
     * which filters it has called the method on to avoid calling lifecycle
     * events twice. Note that if your filter is defined in web.xml, the container
     * will also call lifecycle events. It's recommended that any filters used by
     * {@link ConfigurableFilterStack} be completely removed from web.xml, and
     * only referenced by Spring.
     */
    public void setExecuteLifecycleEvents(boolean executeLifecycleEvents) {
        this.executeLifecycleEvents = executeLifecycleEvents;
    }

    public void afterPropertiesSet() {
        if (parser == null) {
            parser = new FilterMappingParserImpl();
        }
        for (FilterStackSet set : filterSets) {
            set.setFilterMappingParser(parser);
        }
    }

    public List<FilterStackSet> getFilterSets() {
        return filterSets;
    }

    @VisibleForTesting
    LoadingCache<String, CompositeFilter> getCache() {
        return cache;
    }
    
    /**
     * Builds a list of filters to run for a given URL. Is called automatically
     * by the cache when an entry doesn't exist.
     */
    private final class FilterChainFactory implements CacheLoader<String, CompositeFilter> {
        @Nonnull
        @Override
        public CompositeFilter load(@Nonnull String key) {
            List<Filter> filtersToRun = new ArrayList<>();
            for (FilterStackSet set : filterSets) {
                if (set.isMatch(key)) {
                    filtersToRun.add(set.getCompositeFilter());
                }
            }
            return new CompositeFilter(filtersToRun);
        }
    }
}
