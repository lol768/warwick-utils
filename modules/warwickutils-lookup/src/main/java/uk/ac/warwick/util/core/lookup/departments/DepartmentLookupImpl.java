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
import uk.ac.warwick.util.web.Uri;

import java.io.IOException;
import java.util.*;

public class DepartmentLookupImpl implements DepartmentLookup, CacheEntryFactory<String, LinkedHashMap<String, Department>>, DepartmentNameLookup {

    private static final String CACHE_NAME = "departments";
    private static final String DEPTS_KEY = "all.departments";
    private static final long MAX_CACHE_AGE_SECS = 60 * 60 * 24 * 7; // 7 days
    private static final Logger LOGGER = Logger.getLogger(DepartmentLookupImpl.class);

    private static final Uri DEFAULT_DEPARTMENTS_API_URL = Uri.parse("https://departments.warwick.ac.uk/public/api/department.json");
    private static final Uri DEFAULT_FACULTY_API_URL = Uri.parse("https://departments.warwick.ac.uk/public/api/faculty.json");

    static final String ACADEMIC = "ACADEMIC";
    static final String RESERVED = "RESERVED";
    static final String SERVICE = "SERVICE";
    static final String SELF_FINANCING = "SELF_FINANCING";


    private final Cache<String, LinkedHashMap<String, Department>> cache;

    private final String url;
    private final FacultyLookup facultyLookup;

    public DepartmentLookupImpl() {
        this(DEFAULT_DEPARTMENTS_API_URL.toString(), DEFAULT_FACULTY_API_URL.toString());
    }

    public DepartmentLookupImpl(final String url, final String facultyUrl) {
        this.url = url;
        this.facultyLookup = new FacultyLookupImpl(facultyUrl);
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
    public List<Department> getAllDepartments() {
        try {
            return new ArrayList<>(this.cache.get(DEPTS_KEY).values());
        } catch (CacheEntryUpdateException e) {
            LOGGER.error("Error updating cache, trying to fetch all entries", e);
            return null;
        }
    }

    private Set<Department> filterDepartments(String deptType) {
        List<Department> allDepts = getAllDepartments();
        Set<Department> filteredDepts = new HashSet<Department>();
        for (Department d : allDepts) {
            if(d.getType().equals(deptType)) {
                filteredDepts.add(d);
            }
        }
        return filteredDepts;
    }

    @Override
    public Set<Department> getAllAcademicDepartments() {
        return filterDepartments(ACADEMIC);
    }

    @Override
    public Set<Department> getAllServiceDepartments() {
        return filterDepartments(SERVICE);
    }

    @Override
    public Set<Department> getAllAdminDepartments() {
        return filterDepartments(RESERVED);
    }

    @Override
    public Set<Department> getAllSelfFundingDepartments() {
        return filterDepartments(SELF_FINANCING);
    }

    private Department parseJsonToDepartment(final JSONObject json) throws JSONException {
        Department d = new Department();
        d.setCode(json.getString("code"));
        d.setName(json.getString("name"));
        d.setShortName(json.getString("shortName"));
        d.setType(json.getString("type"));
        d.setCurrent(json.getBoolean("inUse"));
        d.setLastModified(new Date(json.getLong("lastModified")));
        d.setFaculty(facultyLookup.getFaculty(json.getString("faculty")));
        return d;
    }

    @Override
    public LinkedHashMap<String, Department> create(String ignored) throws CacheEntryUpdateException {
        HttpGet get = new HttpGet(this.url);
        try {
            LOGGER.info("Updating departments from " + this.url);

            return new SimpleHttpMethodExecutor(Method.get, this.url).execute(new ResponseHandler<LinkedHashMap<String, Department>>() {
                @Override
                public LinkedHashMap<String, Department> handleResponse(HttpResponse response) throws IOException {
                    HttpEntity entity = response.getEntity();

                    try {
                        JSONArray jsonDepartments = new JSONArray(EntityUtils.toString(entity));

                        ArrayList<Department> departments = new ArrayList<>();

                        for (int i = 0; i < jsonDepartments.length(); i++) {
                            JSONObject jsonDepartment = jsonDepartments.getJSONObject(i);
                            Department department = parseJsonToDepartment(jsonDepartment);

                            departments.add(department);
                        }

                        Collections.sort(departments);

                        LinkedHashMap<String, Department> departmentsMap = new LinkedHashMap<>();

                        for (Department department : departments) {
                            departmentsMap.put(department.getCode(), department);
                        }

                        return departmentsMap;
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
    public Map<String, LinkedHashMap<String, Department>> create(List<String> keys) throws CacheEntryUpdateException {
        throw new CacheEntryUpdateException("Unsupported cache operation");
    }

    @Override
    public boolean isSupportsMultiLookups() {
        return false;
    }

    @Override
    public boolean shouldBeCached(LinkedHashMap<String, Department> val) {
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
