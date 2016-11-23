package uk.ac.warwick.util.core.lookup.departments;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class Faculty implements Serializable, Comparable<Faculty> {

    private String code;

    private String name;

    private boolean current;

    private Set<Department> departments;

    private Date lastModified;

    public Faculty() {}

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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int compareTo(Faculty o) {
        return this.getName().compareTo(o.getName());
    }

}
