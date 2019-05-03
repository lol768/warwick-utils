package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.request.PushNotification;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MyWarwickService {
    CompletableFuture<List<Response>> sendAsActivity(Activity activity);
    CompletableFuture<List<Response>> sendAsActivity(Activity activity, int maxAttempts);
    CompletableFuture<List<Response>> sendAsNotification(Activity activity);
    CompletableFuture<List<Response>> sendAsNotification(Activity activity, int maxAttempts);

    /**
     * Sending transient push notifications may require your 'Provider'
     * to have additional permission from the My Warwick app instance
     */
    CompletableFuture<List<Response>> sendAsTransientPush(PushNotification pushNotification);
    CompletableFuture<List<Response>> sendAsTransientPush(PushNotification pushNotification, int maxAttempts);
}
