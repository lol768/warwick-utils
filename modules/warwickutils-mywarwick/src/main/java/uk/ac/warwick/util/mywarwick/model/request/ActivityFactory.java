package uk.ac.warwick.util.mywarwick.model.request;

import javax.validation.constraints.NotNull;
import java.util.Set;

public interface ActivityFactory {

    Activity newActivity();

    Activity newActivity(@NotNull String userId, @NotNull String title, String url, String text, @NotNull String type);

    Activity newActivity(@NotNull Set<String> userIds, @NotNull String title, String url, String text, @NotNull String type);

    Activity newActivity(@NotNull Set<String> userIds, @NotNull Set<String> groups, @NotNull String title, String url, String text, @NotNull String type);

}
