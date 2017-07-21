package uk.ac.warwick.util.mywarwick.model.request;

import uk.ac.warwick.userlookup.GroupService;
import uk.ac.warwick.userlookup.UserLookupInterface;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Named(value = "myWarwickActivityFactory")
public class ActivityFactory {

    private final UserLookupInterface userLookupInterface;

    private final GroupService groupService;

    @Inject
    private ActivityFactory(UserLookupInterface userLookupInterface, GroupService groupService) {
        super();
        this.userLookupInterface = userLookupInterface;
        this.groupService = groupService;
    }

    public Activity newActivity() {
        return assignService(new Activity());
    }

    public Activity newActivity(@NotNull String userId, @NotNull String title, String url, String text, @NotNull String type) {
        return assignService(new Activity(userId, title, url, text, type));
    }

    public Activity newActivity(@NotNull Set<String> userIds, @NotNull String title, String url, String text, @NotNull String type) {
        return assignService(new Activity(userIds, title, url, text, type));
    }

    public Activity newActivity(@NotNull Set<String> userIds, @NotNull Set<String> groups, @NotNull String title, String url, String text, @NotNull String type) {
        return assignService(new Activity(userIds, groups, title, url, text, type));
    }

    private Activity assignService(Activity activity) {
        return activity
                .setGroupService(this.groupService)
                .setUserLookupInterface(this.userLookupInterface);
    }
}
