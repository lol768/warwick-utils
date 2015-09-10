package uk.ac.warwick.util.web.filter.stack;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.common.collect.ImmutableList;

/**
 * Servlet Filter which takes a list of other Filters as constructor,
 * and runs them in order when doFilter is called.
 * <p>
 * <i>Doesn't</i> call {@link Filter#init(FilterConfig)} or {@link Filter#destroy()} as
 * this may cause them to be called twice. If you want
 * these lifecycle methods called, then define the filter in web.xml
 * so that the container will call them. Alternatively look at
 * {@link ConfigurableFilterStack} which will also call the events for you.
 * <p>
 * Any CompositeFilter elements in the input are "unwrapped", and their contents
 * placed directly into this filter. Specify false in the second argument to not do this.
 */
public final class CompositeFilter implements Filter {

    private final ImmutableList<Filter> filters;
    
    public CompositeFilter(final List<Filter> input, final boolean unwrap) {
        if (unwrap) {
            ImmutableList.Builder<Filter> result = ImmutableList.builder();
            for (Filter f : input) {
                if (f instanceof CompositeFilter) {
                    result.addAll(((CompositeFilter)f).getFilters());
                } else {
                    result.add(f);
                }
            }
            this.filters = result.build();
        } else {
            if (input instanceof ImmutableList) {
                this.filters = (ImmutableList<Filter>)input;
            } else {
                this.filters = ImmutableList.copyOf(input);
            }
        }
    }
    
    public CompositeFilter(final List<Filter> input) {
        this(input, true);
    }

    /**
     * Process each of the filters in turn.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        ServletRequest request = req;
        ServletResponse response = res;
        
        NestedFilterChain chain = new NestedFilterChain(filters, filterChain);
        chain.doFilter(request, response);
    }
    
    public List<Filter> getFilters() {
        return filters;
    }

    public void init(FilterConfig config) throws ServletException {
        for (Filter filter: filters) {
            filter.init(config);
        }
    }
    public void destroy() {
        for (Filter filter: filters) {
          filter.destroy();
        }
    }

}
