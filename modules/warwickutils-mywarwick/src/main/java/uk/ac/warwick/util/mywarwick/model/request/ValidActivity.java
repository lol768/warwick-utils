package uk.ac.warwick.util.mywarwick.model.request;

public interface ValidActivity {

    String getType();
    String getTitle();
    Tags getTags();
    Recipients getRecipients();

    default boolean isValid() {
        return ((getTags() == null || getTags().isValid()) &&
                (getRecipients().isValid()) &&
                (getType() != null && !getType().isEmpty()) &&
                (getTitle() != null && !getTitle().isEmpty()));
    }
}
