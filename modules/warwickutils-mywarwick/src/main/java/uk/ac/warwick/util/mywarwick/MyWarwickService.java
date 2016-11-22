package uk.ac.warwick.util.mywarwick;
import org.apache.http.HttpResponse;
import uk.ac.warwick.util.mywarwick.model.Activity;
import java.util.List;
import java.util.concurrent.Future;

public interface MyWarwickService {
    List<Future<HttpResponse>> sendAsActivity(Activity activity);
    List<Future<HttpResponse>> sendAsNotification(Activity activity);
}
