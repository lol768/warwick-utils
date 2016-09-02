package uk.ac.warwick.util.core.lookup.departments;

import java.io.Serializable;

public final class Department implements Serializable, Comparable<Department> {

    private final String code;
    private final String name;
    private final String shortName;
    private final String faculty;

    public Department(final String code, final String name, final String shortName, final String faculty) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.faculty = faculty;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFaculty() {
        return faculty;
    }

    @Override
    public int compareTo(Department o) {
        return this.getName().compareTo(o.getName());
    }

}
