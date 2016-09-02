package uk.ac.warwick.util.core.lookup.departments;

import java.util.List;

public interface DepartmentLookup {

    Department getDepartment(final String code);

    List<Department> getAllDepartments();

}
