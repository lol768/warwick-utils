package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.warwick.userlookup.GroupService;
import uk.ac.warwick.userlookup.UserLookupInterface;

import javax.validation.constraints.NotNull;
import java.util.Set;

public interface ValidActivity {

    String getType();

    String getTitle();

    Set<Tag> getTags();

    ValidRecipients getRecipients();

    @JsonIgnore
    default boolean isValid() {
        return ((getTags() == null || getTags().stream().allMatch(ValidTag::isValid)) &&
                (getRecipients().isValid()) &&
                (getType() != null && !getType().isEmpty()) &&
                (getTitle() != null && !getTitle().isEmpty()));
    }

    default boolean isValid(@NotNull UserLookupInterface userLookupInterface) {
        return isValid() && getRecipients().isValid(userLookupInterface);
    }

    default boolean isValid(@NotNull GroupService groupService) {
        return isValid() && getRecipients().isValid(groupService);
    }

    default boolean isValid(@NotNull UserLookupInterface userLookupInterface, @NotNull GroupService groupService) {
        return isValid() && isValid(userLookupInterface) && isValid(groupService);
    }
}
