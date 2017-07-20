package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ValidTag {
    String getName();

    String getValue();

    @JsonIgnore
    default boolean isValid() {
        return ((getName() != null && !getName().isEmpty()) &&
                (getValue() != null && !getValue().isEmpty())
        );
    }
}
