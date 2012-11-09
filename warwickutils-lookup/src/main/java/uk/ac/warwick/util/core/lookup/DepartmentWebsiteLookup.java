package uk.ac.warwick.util.core.lookup;

public interface DepartmentWebsiteLookup {
    
    /**
     * Will return a fully qualified URL (i.e. not just a Sitebuilder page
     * path), or null if we couldn't find a suitable page.
     */
    String getWebsiteForDepartmentCode(String code);

}
