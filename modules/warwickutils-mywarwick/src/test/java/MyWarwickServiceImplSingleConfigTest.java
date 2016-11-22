import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.MyWarwickServiceImpl;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class MyWarwickServiceImplSingleConfigTest {

    Config config = new Config("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");
    MyWarwickServiceImpl myWarwickService = new MyWarwickServiceImpl(config);
    Activity activity = new Activity("id", "title", "url", "text", "fake-type");

    @Test
    public void activityPathShouldBeCorrect() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/activities", myWarwickService.getConfigs().get(0).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrect() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/notifications", myWarwickService.getConfigs().get(0).getNotificationPath());
    }

    @Test
    public void shouldCreateJsonBodyCorrectly() {
        assertEquals(
                new Gson().toJson(activity),
                myWarwickService.makeJsonBody(activity)
        );
    }

    @Test
    public void requestShouldHaveCorrectJsonBody() throws IOException {

        String expected = myWarwickService.makeJsonBody(activity);
        assertEquals(
                expected,
                IOUtils.toString(myWarwickService.makeRequest("", expected, "", "").getEntity().getContent(), Charset.defaultCharset())
        );
    }

    @Test
    public void requestShouldHaveCorrectPath() throws MalformedURLException {
        String expected = "http://test.com";
        assertEquals(
                expected,
                myWarwickService.makeRequest(expected, "", "", "").getURI().toURL().toString()
        );
    }

    @Test
    public void requestShouldHaveCorrectAuthHeader() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), config.getApiUser(), config.getApiPassword())
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentType() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), config.getApiUser(), config.getApiPassword())
                        .getFirstHeader("content-type").getValue()
        );
    }

}
