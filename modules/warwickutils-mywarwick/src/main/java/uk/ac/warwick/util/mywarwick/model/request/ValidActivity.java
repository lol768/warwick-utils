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

    GroupService getGroupService();

    UserLookupInterface getUserLookupInterface();

    ValidRecipients getRecipients();

    @JsonIgnore
    default boolean isValid() {
        return ((getTags() == null || getTags().stream().allMatch(ValidTag::isValid)) &&
                (getRecipients().isValid()) &&
                (getGroupService() == null || getRecipients().isValid(getGroupService())) &&
                (getUserLookupInterface() == null || getRecipients().isValid(getUserLookupInterface())) &&
                (getType() != null && !getType().isEmpty()) &&
                (getTitle() != null && !getTitle().isEmpty()));
    }
}
