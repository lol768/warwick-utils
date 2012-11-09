package uk.ac.warwick.util.core.lookup;

public interface DepartmentNameLookup {

    /**
     * Will return the name of a department with the specified code, or null if
     * we couldn't find one.
     */
    String getNameForDepartmentCode(String code);

}
