package uk.ac.warwick.util.mywarwick.model.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class Recipients {
    private Set<String> users;
    private Set<String> groups;

    public Recipients() {
        this.users = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public Recipients(Set<String> users) {
        this.users = users;
        this.groups = new HashSet<>();
    }


    public Recipients(String user) {
        this();
        this.users.add(user);
    }


    public Recipients(Set<String> users, Set<String> groups) {
        this.users = users;
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Recipients that = (Recipients) o;

        return new EqualsBuilder()
                .append(getUsers(), that.getUsers())
                .append(getGroups(), that.getGroups())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUsers())
                .append(getGroups())
                .toHashCode();
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }
}
