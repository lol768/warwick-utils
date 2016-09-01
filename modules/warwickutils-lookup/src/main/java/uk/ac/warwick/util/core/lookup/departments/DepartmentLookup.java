package uk.ac.warwick.util.core.lookup.departments;

import java.util.Collection;

public interface DepartmentLookup {

    Department getDepartment(final String code);

    Collection<Department> getAllDepartments();

}
