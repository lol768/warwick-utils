package uk.ac.warwick.util.core.lookup.departments;

import java.io.Serializable;
import java.util.Date;

public final class Department implements Serializable, Comparable<Department> {

    private String code;
    private String name;
    private String shortName;
    private String type;
    private boolean current;
    private Faculty faculty;
    private Date lastModified;

    public Department() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }


    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int compareTo(Department o) {
        return this.getName().compareTo(o.getName());
    }

}
