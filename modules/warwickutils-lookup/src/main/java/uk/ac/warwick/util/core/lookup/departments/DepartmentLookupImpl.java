package uk.ac.warwick.util.core.lookup.departments;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryFactory;
import uk.ac.warwick.util.cache.CacheEntryUpdateException;
import uk.ac.warwick.util.cache.Caches;
import uk.ac.warwick.util.core.Logger;
import uk.ac.warwick.util.core.lookup.DepartmentNameLookup;
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor.Method;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentLookupImpl implements DepartmentLookup, CacheEntryFactory<String, HashMap<String, Department>>, DepartmentNameLookup {

    private static final String CACHE_NAME = "departments";
    private static final String DEPTS_KEY = "all.departments";
    private static final long MAX_CACHE_AGE_SECS = 60 * 60 * 24 * 7; // 7 days

    private static final Logger LOGGER = Logger.getLogger(DepartmentLookupImpl.class);

    private final Cache<String, HashMap<String, Department>> cache;

    private final String url;

    public DepartmentLookupImpl(final String url) {
        this.url = url;

        this.cache = Caches.newCache(CACHE_NAME, this, MAX_CACHE_AGE_SECS, Caches.CacheStrategy.InMemoryOnly);

        // Pre-warm cache by immediately fetching a department
        getDepartment("IN");
    }

    public void clearCache() {
        this.cache.clear();
    }

    @Override
    public Department getDepartment(final String code) {
        if (code == null) {
            return null;
        }

        try {
            return this.cache.get(DEPTS_KEY).get(code.toUpperCase());
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Error updating cache, trying to fetch key " + code, e);
            return null;
        }
    }

    @Override
    public Collection<Department> getAllDepartments() {
        try {
            return this.cache.get(DEPTS_KEY).values();
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Error updating cache, trying to fetch all entries", e);
            return null;
        }
    }

    private static Department parseJsonToDepartment(final JSONObject json) throws JSONException {
        return new Department(
                json.get("code").toString(),
                json.get("name").toString(),
                json.get("shortName").toString(),
                json.get("faculty").toString()
        );
    }

    @Override
    public HashMap<String, Department> create(String ignored) throws CacheEntryUpdateException {
        HttpGet get = new HttpGet(this.url);
        try {
            LOGGER.info("Updating departments from " + this.url);

            return new SimpleHttpMethodExecutor(Method.get, this.url).execute(new ResponseHandler<HashMap<String, Department>>() {
                @Override
                public HashMap<String, Department> handleResponse(HttpResponse response) throws IOException {
                    HttpEntity entity = response.getEntity();

                    try {
                        JSONArray depts = new JSONArray(EntityUtils.toString(entity));
                        HashMap<String, Department> deptMap = new HashMap<>();

                        for (int i = 0; i < depts.length(); i++) {
                            JSONObject dept = depts.getJSONObject(i);
                            deptMap.put(dept.get("code").toString(), DepartmentLookupImpl.parseJsonToDepartment(dept));
                        }

                        return deptMap;
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                }
            }).getRight();
        } catch (IOException e) {
            LOGGER.error("Unable to fetch department information from " + get.getURI().toString(), e.getMessage());
            throw new CacheEntryUpdateException(e);
        }
    }

    @Override
    public Map<String, HashMap<String, Department>> create(List<String> keys) throws CacheEntryUpdateException {
        throw new CacheEntryUpdateException("Unsupported cache operation");
    }

    @Override
    public boolean isSupportsMultiLookups() {
        return false;
    }

    @Override
    public boolean shouldBeCached(HashMap<String, Department> val) {
        return true;
    }

    @Override
    public String getNameForDepartmentCode(String code) {
        Department department = getDepartment(code);

        if (department != null) {
            return department.getName();
        }

        return null;
    }

}
