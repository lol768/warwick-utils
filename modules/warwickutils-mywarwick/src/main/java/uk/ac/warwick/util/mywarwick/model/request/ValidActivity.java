package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public interface ValidActivity {

    String getType();
    String getTitle();
    Set<Tag> getTags();
    Recipients getRecipients();

    @JsonIgnore
    default boolean isValid() {
        return ((getTags() == null || getTags().stream().allMatch(ValidTag::isValid)) &&
                (getRecipients().isValid()) &&
                (getType() != null && !getType().isEmpty()) &&
                (getTitle() != null && !getTitle().isEmpty()));
    }
}
