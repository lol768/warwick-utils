package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.warwick.userlookup.GroupService;
import uk.ac.warwick.userlookup.UserLookupInterface;
import uk.ac.warwick.userlookup.webgroups.GroupNotFoundException;
import uk.ac.warwick.userlookup.webgroups.GroupServiceException;

import javax.validation.constraints.NotNull;
import java.util.Set;

public interface ValidRecipients {

    Set<String> getUsers();

    Set<String> getGroups();

    @JsonIgnore
    default boolean isValid() {
        Set<String> users = getUsers();
        Set<String> groups = getGroups();
        boolean isValid = !(users.isEmpty() && groups.isEmpty()) && isValid(users) && isValid(groups);
        return isValid;
    }

    default boolean isValid(@NotNull UserLookupInterface userLookupInterface) {
        return isValid() && getUsers().stream().allMatch(e -> isValid(userLookupInterface, e));
    }

    default boolean isValid(@NotNull GroupService groupService) {
        return isValid() && getGroups().stream().allMatch(e -> isValid(groupService, e));
    }

    default boolean isValid(@NotNull UserLookupInterface userLookupInterface, @NotNull GroupService groupService) {
        return isValid(userLookupInterface) && isValid(groupService);
    }

    default boolean isValid(@NotNull UserLookupInterface userLookupInterface, String userId) {
        return userLookupInterface.getUserByWarwickUniId(userId, false).isFoundUser();
    }

    default boolean isValid(@NotNull GroupService groupService, String groupName) {
        try {
            return groupService.getGroupByName(groupName) != null;
        } catch (GroupNotFoundException | GroupServiceException e) {
            return false;
        }
    }

    default boolean isValid(String text) {
        return text != null && !text.isEmpty() && !text.contains(" ");
    }

    default boolean isValid(Set<String> texts) {
        return texts.stream().allMatch(this::isValid);
    }
}
