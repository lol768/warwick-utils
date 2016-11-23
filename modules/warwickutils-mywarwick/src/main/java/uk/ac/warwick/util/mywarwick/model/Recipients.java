package uk.ac.warwick.util.mywarwick.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recipients {
    private Set<String> users;

    public Recipients() {
        this.users = new HashSet<>();
    }

    public Recipients(Set<String> users) {
        this.users = users;
    }

    public Recipients(String user) {
        this();
        this.users.add(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Recipients that = (Recipients) o;

        return new EqualsBuilder()
                .append(getUsers(), that.getUsers())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUsers())
                .toHashCode();
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
}
