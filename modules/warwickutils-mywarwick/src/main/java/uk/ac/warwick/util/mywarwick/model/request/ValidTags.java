package uk.ac.warwick.util.mywarwick.model.request;

import java.util.Set;

public interface ValidTags {
    Set<Tag> getTags();

    default boolean isValid() {
        return getTags().stream().allMatch(ValidTag::isValid);
    }
}
