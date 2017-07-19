package uk.ac.warwick.util.mywarwick.model.request;

import java.util.Set;

public interface ValidRecipients {

    Set<String> getUsers();
    Set<String> getGroups();

    default boolean isValid() {
        Set<String> users = getUsers();
        Set<String> groups = getGroups();
        boolean isValid = !(users.isEmpty() && groups.isEmpty()) && isValid(users) && isValid(groups);
        return isValid;
    }

    default boolean isValid(String text) {
        return text != null && !text.isEmpty() && !text.contains(" ");
    }

    default boolean isValid(Set<String> texts) {
        return texts.stream().allMatch(this::isValid);
    }
}
