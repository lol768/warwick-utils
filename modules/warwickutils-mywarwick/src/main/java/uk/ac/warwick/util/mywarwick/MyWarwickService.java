package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.Activity;

public interface MyWarwickService {
    void sendAsActivity(Activity activity);
    void sendAsNotification(Activity activity);
}
