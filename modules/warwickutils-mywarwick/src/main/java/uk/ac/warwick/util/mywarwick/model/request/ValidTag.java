package uk.ac.warwick.util.mywarwick.model.request;

public interface ValidTag {
    String getDisplay_value();
    String getName();
    String getValue();

    default boolean isValid() {
        return ((getDisplay_value() != null && !getDisplay_value().isEmpty()) &&
                (getName() != null && !getName().isEmpty()) &&
                (getValue() != null && !getValue().isEmpty())
        );
    }
}
