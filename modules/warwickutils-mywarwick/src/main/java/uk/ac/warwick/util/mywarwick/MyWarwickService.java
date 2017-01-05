package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MyWarwickService {
    CompletableFuture<List<Response>> sendAsActivity(Activity activity);
    CompletableFuture<List<Response>> sendAsNotification(Activity activity);
}
