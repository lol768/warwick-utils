package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import java.util.List;
import java.util.concurrent.Future;

public interface MyWarwickService {
    Future<List<Response>> sendAsActivity(Activity activity);
    Future<List<Response>> sendAsNotification(Activity activity);
}
