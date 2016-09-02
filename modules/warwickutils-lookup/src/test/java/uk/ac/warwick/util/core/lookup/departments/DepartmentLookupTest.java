package uk.ac.warwick.util.core.lookup.departments;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class DepartmentLookupTest {

    private DepartmentLookupImpl departmentLookup;

    @Before
    public void setUp() {
        departmentLookup = new DepartmentLookupImpl("https://departments.warwick.ac.uk/public/api/department.json");
    }

    @Test
    public void testGetDepartment() throws Exception {
        Department department = departmentLookup.getDepartment("IN");

        assertEquals("IN", department.getCode());
        assertEquals("IT Services", department.getName());
        assertEquals("IT Services", department.getShortName());
        assertEquals("X", department.getFaculty());
    }

    @Test
    public void testGetNonExistentDepartment() throws Exception {
        Department department = departmentLookup.getDepartment("XYZ");

        assertNull(department);
    }

    @Test
    public void testGetNameForDepartmentCode() throws Exception {
        String name = departmentLookup.getNameForDepartmentCode("IN");

        assertEquals("IT Services", name);
    }

    @Test
    public void testGetNameForNonExistentDepartmentCode() throws Exception {
        String name = departmentLookup.getNameForDepartmentCode("XYZ");

        assertNull(name);
    }

    @Test
    public void testGetNullDepartment() throws Exception {
        Department department = departmentLookup.getDepartment(null);

        assertNull(department);
    }

    @Test
    public void testGetAllDepartments() throws Exception {
        List<Department> allDepartments = departmentLookup.getAllDepartments();

        assertFalse(allDepartments.isEmpty());

        // Assert that the list of departments is sorted by the ordering on Department
        Department lastDepartment = null;

        for (Department department : allDepartments) {
            if (lastDepartment != null) {
                assertTrue(lastDepartment.compareTo(department) <= 0);
            }

            lastDepartment = department;
        }
    }

    @Test
    public void testGetDepartmentWithBadSource() throws Exception {
        // Not really a department source
        departmentLookup = new DepartmentLookupImpl("https://websignon-test.warwick.ac.uk/department.json");
        departmentLookup.clearCache();

        assertNull(departmentLookup.getDepartment("IN"));
        assertNull(departmentLookup.getAllDepartments());
    }

}
