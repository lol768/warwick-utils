package uk.ac.warwick.util.mywarwick;
import uk.ac.warwick.util.mywarwick.model.Activity;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface MyWarwickService {
    Integer sendAsActivity(Activity activity) throws ExecutionException, InterruptedException;
    Integer sendAsNotification(Activity activity) throws ExecutionException, InterruptedException;

    void sendAsActivities(Set<Activity> activities);
    void sendAsNotifications(Set<Activity> activities);
}
