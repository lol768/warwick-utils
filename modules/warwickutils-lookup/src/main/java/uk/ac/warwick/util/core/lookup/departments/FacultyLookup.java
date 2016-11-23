package uk.ac.warwick.util.core.lookup.departments;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface FacultyLookup {

    Faculty getFaculty(final String code);

    List<Faculty> getAllFaculties();

}