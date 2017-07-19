package uk.ac.warwick.util.mywarwick.model.request;

public interface ValidTag {
    String getName();
    String getValue();

    default boolean isValid() {
        return ((getName() != null && !getName().isEmpty()) &&
                (getValue() != null && !getValue().isEmpty())
        );
    }
}
