package uk.ac.warwick.util.core.lookup;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.httpclient.HttpMethodExecutor.Method;
import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.UriBuilder;

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
public final class GoWarwickDepartmentWebsiteLookup implements DepartmentWebsiteLookup {
    
    private static final Logger LOGGER = Logger.getLogger(GoWarwickDepartmentWebsiteLookup.class);
    
    public static final String CACHE_NAME = "DepartmentWebsiteCache";
    
    private static final Uri DEFAULT_GO_API_URL = Uri.parse("http://sitebuilder.warwick.ac.uk/sitebuilder2/api/go/redirect.json");
    
    private static final long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 24; // Cache for one day
    
    private final Cache<String, String> cache;
    
    public GoWarwickDepartmentWebsiteLookup() {
        this(DEFAULT_GO_API_URL);
    }

    /**
     * EhCache is REQUIRED here, to make sure that the caches have been properly
     * configured with a disk store. See ehcache-default.xml
     */
    public GoWarwickDepartmentWebsiteLookup(Uri goApiUrl) {
        this.cache = Caches.newCache(CACHE_NAME, new WebsiteLookupEntryFactory(goApiUrl), DEFAULT_CACHE_TIMEOUT, CacheStrategy.EhCacheRequired);
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
        
        private final Uri apiUrl;
        
        public WebsiteLookupEntryFactory(Uri goApiUrl) {
            this.apiUrl = goApiUrl;
        }
        
        public String create(String code) throws CacheEntryUpdateException {
            String goPath = "dep-code-" + code;
            
            HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
            ex.setUrl(new UriBuilder(apiUrl).addQueryParameter("path", goPath).toUri());
            
            try {
                int statusCode = ex.execute();
                
                if (statusCode != HttpServletResponse.SC_OK) {
                    throw new CacheEntryUpdateException("Expected SC_OK but returned " + statusCode);
                }
                
                JSONObject obj = new JSONObject(ex.retrieveContentsAsString());
                boolean isFound = obj.getBoolean("found");
                
                if (isFound) {
                    JSONObject redirect = obj.getJSONObject("redirect");
                    
                    return redirect.getString("target");
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