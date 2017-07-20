package uk.ac.warwick.util.mywarwick.model.request;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

public class Recipients implements ValidRecipients {
    private Set<String> users;
    private Set<String> groups;

    public Recipients() {
        this.users = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public Recipients(@NotNull Set<String> users) {
        this.users = users;
        this.groups = new HashSet<>();
    }


    public Recipients(@NotNull String user) {
        this();
        this.users.add(user);
    }


    public Recipients(@NotNull Set<String> users, @NotNull Set<String> groups) {
        this.users = users;
        this.groups = groups;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(@NotNull Set<String> users) {
        this.users = users;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(@NotNull Set<String> groups) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("users", users)
                .append("groups", groups)
                .toString();
    }
}
