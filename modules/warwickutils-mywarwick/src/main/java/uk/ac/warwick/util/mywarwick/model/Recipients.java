package uk.ac.warwick.util.mywarwick.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recipients {
    Set<String> users;

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
        if (!(o instanceof Recipients)) return false;

        Recipients that = (Recipients) o;

        return getUsers().equals(that.getUsers());

    }

    @Override
    public int hashCode() {
        return getUsers().hashCode();
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
}
