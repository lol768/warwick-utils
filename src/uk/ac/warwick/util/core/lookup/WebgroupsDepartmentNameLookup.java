package uk.ac.warwick.util.core.lookup;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;

import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.cache.Caches.CacheStrategy;
import uk.ac.warwick.util.cache.SingularCacheEntryFactory;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;
import uk.ac.warwick.util.web.Uri;

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
public final class WebgroupsDepartmentNameLookup implements DepartmentNameLookup {
    
    private static final Logger LOGGER = Logger.getLogger(WebgroupsDepartmentNameLookup.class);
    
    public static final String CACHE_NAME = "DepartmentNameCache";
    
    private static final Uri DEFAULT_WEBGROUPS_API_URL = Uri.parse("http://webgroups.warwick.ac.uk/query/department/all");
    
    private static final long DEFAULT_CACHE_TIMEOUT = 60 * 60 * 24 * 7; // Cache for one week
    
    private final Cache<String, String> cache;
    
    public WebgroupsDepartmentNameLookup() {
        this(DEFAULT_WEBGROUPS_API_URL);
    }

    /**
     * EhCache is REQUIRED here, to make sure that the caches have been properly
     * configured with a disk store. See ehcache-default.xml
     */
    public WebgroupsDepartmentNameLookup(Uri webgroupsApiUrl) {
        this.cache = Caches.newCache(CACHE_NAME, new NameLookupEntryFactory(webgroupsApiUrl), DEFAULT_CACHE_TIMEOUT, CacheStrategy.EhCacheRequired);
    }

    /**
     * Will return a fully qualified URL (i.e. not just a Sitebuilder page
     * path), or null if we couldn't find a suitable page.
     */
    public String getNameForDepartmentCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        
        code = code.toLowerCase().trim();
        
        try {
            return cache.get(code);
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Exception getting department name; returning null for now (but not storing null)");
            return null;
        }
    }
    
    public static final class NameLookupEntryFactory extends SingularCacheEntryFactory<String, String> {
        
        private final Uri apiUrl;
        
        public NameLookupEntryFactory(Uri webgroupsApiUrl) {
            this.apiUrl = webgroupsApiUrl;
        }
        
        @SuppressWarnings("unchecked")
        public String create(String code) throws CacheEntryUpdateException {           
            HttpMethodExecutor ex = new SimpleHttpMethodExecutor(Method.get);
            ex.setUrl(apiUrl);
            
            try {
                Document document = ex.execute(new ResponseHandler<Document>() {
                    public Document handleResponse(HttpResponse response) throws IOException {
                        if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                            throw new IOException(new CacheEntryUpdateException("Could not access webgroups server: " + response.getStatusLine().getStatusCode()));
                        }
                        
                        try {
                            return new SAXReader().read(response.getEntity().getContent());
                        } catch (DocumentException e) {
                            throw new IOException(new CacheEntryUpdateException("Invalid XML from webgroups server", e));
                        }
                    }
                }).getRight();
                
                for (Text text : (Iterable<Text>)document.createXPath("/departments/department/code/text()").selectNodes(document)) {
                    if (text.getText().trim().equalsIgnoreCase(code)) {
                        Text departmentName = (Text) text.getParent().getParent().selectSingleNode("name/text()");
                        if (departmentName == null) {
                            return null;
                        } else {
                            return departmentName.getText();
                        }
                    }
                }
                
                return null;
            } catch (Exception e) {
                LOGGER.error("Exception updating department name cache", e);
                
                if (e instanceof CacheEntryUpdateException) {
                    throw (CacheEntryUpdateException) e;
                } else if (e.getCause() != null && e.getCause() instanceof CacheEntryUpdateException) {
                    throw (CacheEntryUpdateException) e.getCause();
                }
                
                throw new CacheEntryUpdateException(e);
            }
        }

        public boolean shouldBeCached(String val) {
            return true; // always cache
        }
        
    }

}
