package uk.ac.warwick.util.core.lookup;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor.Method;

/**
 * Given a department code, will lookup the website related to that department
 * from the Go API service, assuming that there is a redirect existing at
 * dep-code-XX where XX is the department code.
 * <p>
 * Expected to fail gracefully and will use a Cache to store the results of the
 * lookup.
 * 
 * @author Mat
 */
public final class DepartmentWebsiteLookup {
    
    private static final Logger LOGGER = Logger.getLogger(DepartmentWebsiteLookup.class);
    
    public static final String CACHE_NAME = "DepartmentWebsiteCache";
    
    private static final String DEFAULT_GO_API_URL = "http://sitebuilder.warwick.ac.uk/sitebuilder2/api/go/redirect.json?path=";
    
    private static final long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 24; // Cache for one day
    
    private final Cache<String, String> cache;
    
    public DepartmentWebsiteLookup() {
        this(DEFAULT_GO_API_URL);
    }
    
    public DepartmentWebsiteLookup(String goApiUrl) {
        this.cache = Caches.newCache(CACHE_NAME, new WebsiteLookupEntryFactory(goApiUrl), DEFAULT_CACHE_TIMEOUT);
    }

    /**
     * Will return a fully qualified URL (i.e. not just a Sitebuilder page
     * path), or null if we couldn't find a suitable page.
     */
    public String getWebsiteForDepartmentCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        
        code = code.toLowerCase().trim();
        
        try {
            return cache.get(code);
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Exception getting departmental website; returning null for now (but not storing null)");
            return null;
        }
    }
    
    public static final class WebsiteLookupEntryFactory extends SingularCacheEntryFactory<String, String> {
        
        private final String apiUrl;
        
        private final JSONParser parser = new JSONParser();
        
        public WebsiteLookupEntryFactory(String goApiUrl) {
            this.apiUrl = goApiUrl;
        }
        
        @SuppressWarnings("unchecked")
        public String create(String code) throws CacheEntryUpdateException {
            String goPath = "dep-code-" + code;
            
            HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
            ex.setUrl(apiUrl + goPath);
            
            try {
                int statusCode = ex.execute();
                
                if (statusCode != HttpServletResponse.SC_OK) {
                    throw new CacheEntryUpdateException("Expected SC_OK but returned " + statusCode);
                }
                
                Map<String, Object> obj = (Map<String, Object>)parser.parse(ex.retrieveContentsAsString());
                boolean isFound = ((Boolean)obj.get("found")).booleanValue();
                
                if (isFound) {
                    Map<String, Object> redirect = (Map<String, Object>)obj.get("redirect");
                    
                    return (String) redirect.get("target");
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new CacheEntryUpdateException(e);
            } finally {
                ex.close();
            }
        }

        public boolean shouldBeCached(String val) {
            return true; // always cache
        }
        
    }

}
