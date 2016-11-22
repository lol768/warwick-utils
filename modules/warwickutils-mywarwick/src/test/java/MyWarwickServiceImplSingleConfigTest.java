import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.MyWarwickService;
import uk.ac.warwick.util.mywarwick.MyWarwickServiceImpl;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MyWarwickServiceImplSingleConfigTest {

    Config config = new Config("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");
    MyWarwickServiceImpl myWarwickService = new MyWarwickServiceImpl(config);
    Activity activity = new Activity("id", "title", "url", "text", "fake-type");

    @Test
    public void baseUrlShouldBeCorrect() {
        assertEquals("https://fake.com", myWarwickService.getConfigs().get(0).getBaseUrl());
    }

    @Test
    public void providerIdShouldBeCorrect() {
        assertEquals("fakeProviderId", myWarwickService.getConfigs().get(0).getProviderId());
    }

    @Test
    public void providerUserNameShouldBeCorrect() {
        assertEquals("shylock-mywarwick-api-user", myWarwickService.getConfigs().get(0).getApiUser());
    }

    @Test
    public void providerPasswordShouldBeCorrect() {
        assertEquals("blinking", myWarwickService.getConfigs().get(0).getApiPassword());
    }

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
                new ObjectMapper().convertValue(activity, JsonNode.class),
                myWarwickService.makeJsonBody(activity)
        );
    }

    @Test
    public void requestShouldHaveCorrectJsonBody() throws IOException {

        assertEquals(
                myWarwickService.makeJsonBody(activity),
                new ObjectMapper().readTree(new ObjectMapper().convertValue(myWarwickService.makeRequest("", myWarwickService.makeJsonBody(activity),config.getApiUser(),config.getApiPassword()).getBody().toString(), JsonNode.class).asText())
        );
    }

    @Test
    public void requestShouldHaveCorrectPath() {
        assertEquals(
                "http://test.com",
                myWarwickService.makeRequest("http://test.com", myWarwickService.makeJsonBody(new Activity("id", "title", "url", "text", "fake-type")),config.getApiUser(),config.getApiPassword()).getHttpRequest().getUrl()
        );
    }

    @Test
    public void requestShouldHaveCorrectAuthHeader() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity),config.getApiUser(),config.getApiPassword())
                        .getHttpRequest()
                        .getHeaders()
                        .get("Authorization")
                        .get(0)
        );
    }

    @Test
    public void requestShouldHaveCorrectContentType() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity),config.getApiUser(),config.getApiPassword())
                        .getHttpRequest()
                        .getHeaders()
                        .get("content-type")
                        .get(0)
        );
    }

}
