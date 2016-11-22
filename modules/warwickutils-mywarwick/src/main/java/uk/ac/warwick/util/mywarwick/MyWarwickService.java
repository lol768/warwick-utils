package uk.ac.warwick.util.mywarwick;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Response;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MyWarwickService {

    // default sending to prod
    List<Response> sendAsActivity(Activity activity) throws ExecutionException, InterruptedException;
    List<Response> sendAsNotification(Activity activity) throws ExecutionException, InterruptedException;
}
