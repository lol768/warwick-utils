package uk.ac.warwick.util.core.lookup.departments;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class FacultyLookupTest {

    private FacultyLookupImpl facultyLookup;

    @Before
    public void setUp() {
        facultyLookup = new FacultyLookupImpl("https://departments.warwick.ac.uk/public/api/faculty.json");
    }

    @Test
    public void testGetFaculty() throws Exception {
        Faculty f = facultyLookup.getFaculty("X");
        assertEquals("X", f.getCode());
        assertEquals("Administration and Service", f.getName());
        assertEquals(true, f.isCurrent());
        assertNotNull(f.getLastModified());
    }

    @Test
    public void testGetNonExistentFaculty() throws Exception {
        Faculty f = facultyLookup.getFaculty("HERONZ");
        assertNull(f);
    }

    @Test
    public void testGetNullFaculty() throws Exception {
        Faculty f = facultyLookup.getFaculty(null);
        assertNull(f);
    }

    @Test
    public void testGetAllFaculties() throws Exception {
        List<Faculty> allFaculties = facultyLookup.getAllFaculties();

        assertFalse(allFaculties.isEmpty());

        // Assert that the list of departments is sorted by the ordering on Department
        Faculty lastFaculty = null;
        for (Faculty f : allFaculties) {
            if (lastFaculty != null) {
                assertTrue(lastFaculty.compareTo(f) <= 0);
            }
            lastFaculty = f;
        }
    }

    @Test
    public void testGetFacultyWithBadSource() throws Exception {
        // Not really a department source
        facultyLookup = new FacultyLookupImpl("https://websignon-test.warwick.ac.uk/faculty.json");
        facultyLookup.clearCache();

        assertNull(facultyLookup.getFaculty("IN"));
        assertNull(facultyLookup.getAllFaculties());
    }
}
