package uk.ac.warwick.util.mywarwick.model.request;

import javax.validation.constraints.NotNull;
import java.util.Set;

public interface ActivityFactory {

    public Activity newActivity();

    public Activity newActivity(@NotNull String userId, @NotNull String title, String url, String text, @NotNull String type);

    public Activity newActivity(@NotNull Set<String> userIds, @NotNull String title, String url, String text, @NotNull String type);

    public Activity newActivity(@NotNull Set<String> userIds, @NotNull Set<String> groups, @NotNull String title, String url, String text, @NotNull String type);

}
