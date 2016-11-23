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
import uk.ac.warwick.util.httpclient.httpclient4.HttpMethodExecutor;
import uk.ac.warwick.util.httpclient.httpclient4.SimpleHttpMethodExecutor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FacultyLookupImpl implements FacultyLookup, CacheEntryFactory<String, LinkedHashMap<String, Faculty>> {

    private static final String CACHE_NAME = "faculties";
    private static final String FACULTIES_KEY = "all.faculties";
    private static final long MAX_CACHE_AGE_SECS = 60 * 60 * 24 * 7; // 7 days
    private static final SimpleDateFormat LAST_MODIFIED_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger LOGGER = Logger.getLogger(FacultyLookupImpl.class);

    private final Cache<String, LinkedHashMap<String, Faculty>> cache;

    private final String url;

    public FacultyLookupImpl(final String url) {
        this.url = url;
        this.cache = Caches.newCache(CACHE_NAME, this, MAX_CACHE_AGE_SECS, Caches.CacheStrategy.InMemoryOnly);
        // Pre-warm cache by immediately fetching a department
        getFaculty("X");
    }

    public void clearCache() {
        this.cache.clear();
    }

    @Override
    public Faculty getFaculty(final String code) {
        if (code == null) {
            return null;
        }
        try {
            return this.cache.get(FACULTIES_KEY).get(code.toUpperCase());
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Error updating cache, trying to fetch key " + code, e);
            return null;
        }
    }

    @Override
    public List<Faculty> getAllFaculties() {
        try {
            return new ArrayList<>(this.cache.get(FACULTIES_KEY).values());
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Error updating cache, trying to fetch all entries", e);
            return null;
        }
    }

    private static Faculty parseJsonToFaculty(final JSONObject json) throws JSONException {
        Faculty f = new Faculty();
        f.setCode(json.getString("code"));
        f.setName(json.getString("name"));
        f.setCurrent(json.getBoolean("inUse"));
        f.setLastModified(new Date(json.getLong("lastModified")));
        return f;
    }

    @Override
    public LinkedHashMap<String, Faculty> create(String ignored) throws CacheEntryUpdateException {
        HttpGet get = new HttpGet(this.url);
        try {
            LOGGER.info("Updating faculties from " + this.url);

            return new SimpleHttpMethodExecutor(HttpMethodExecutor.Method.get, this.url).execute(new ResponseHandler<LinkedHashMap<String, Faculty>>() {
                @Override
                public LinkedHashMap<String, Faculty> handleResponse(HttpResponse response) throws IOException {
                    HttpEntity entity = response.getEntity();

                    try {
                        JSONArray jsonFaculties = new JSONArray(EntityUtils.toString(entity));

                        ArrayList<Faculty> faculties = new ArrayList<>();

                        for (int i = 0; i < jsonFaculties.length(); i++) {
                            JSONObject jsonFaculty = jsonFaculties.getJSONObject(i);
                            Faculty f = FacultyLookupImpl.parseJsonToFaculty(jsonFaculty);
                            faculties.add(f);
                        }

                        Collections.sort(faculties);

                        LinkedHashMap<String, Faculty> facultyMap = new LinkedHashMap<>();

                        for (Faculty f : faculties) {
                            facultyMap.put(f.getCode(), f);
                        }

                        return facultyMap;
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
    public Map<String, LinkedHashMap<String, Faculty>> create(List<String> keys) throws CacheEntryUpdateException {
        throw new CacheEntryUpdateException("Unsupported cache operation");
    }

    @Override
    public boolean isSupportsMultiLookups() {
        return false;
    }

    @Override
    public boolean shouldBeCached(LinkedHashMap<String, Faculty> val) {
        return true;
    }
}
