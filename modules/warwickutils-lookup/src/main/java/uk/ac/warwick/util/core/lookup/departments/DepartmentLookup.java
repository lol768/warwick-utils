package uk.ac.warwick.util.core.lookup.departments;

import java.util.List;
import java.util.Set;

public interface DepartmentLookup {

    Department getDepartment(final String code);

    List<Department> getAllDepartments();

    Set<Department> getAllAcademicDepartments();

    Set<Department> getAllServiceDepartments();

    Set<Department> getAllAdminDepartments();

    Set<Department> getAllSelfFundingDepartments();
}
