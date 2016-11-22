package uk.ac.warwick.util.mywarwick.model;

import java.util.ArrayList;
import java.util.List;

public class Recipients {
    List<String> users;

    public Recipients() {
        this.users = new ArrayList<>();
    }

    public Recipients(List<String> users) {
        this.users = users;
    }

    public Recipients(String user) {
        this();
        this.users.add(user);
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
