import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.warwick.util.mywarwick.MyWarwickServiceImpl;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickServiceImplMultiConfigTest {
    @Mock
    List<Config> configs;

    @Mock
    MyWarwickServiceImpl myWarwickService;

    @Mock
    Activity activity;

    @Before
    public void setUp(){
        Config config1 = new Config("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");
        Config config2 = new Config("https://ekaf.com", "fakerProviderId", "moonwalker-api-user", "hanging");
        configs = new ArrayList<>();
        configs.add(config1);
        configs.add(config2);
        myWarwickService = new MyWarwickServiceImpl(configs);
        activity = new Activity("id", "title", "url", "text", "fake-type");
    }


    @Test
    public void activityPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/activities", myWarwickService.getConfigs().get(0).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/notifications", myWarwickService.getConfigs().get(0).getNotificationPath());
    }


    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig1() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity),configs.get(0).getApiUser(),configs.get(0).getApiPassword())
                        .getHttpRequest()
                        .getHeaders()
                        .get("Authorization")
                        .get(0)
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig1() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity),configs.get(0).getApiUser(),configs.get(0).getApiPassword())
                        .getHttpRequest()
                        .getHeaders()
                        .get("content-type")
                        .get(0)
        );
    }

    @Test
    public void activityPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/activities", myWarwickService.getConfigs().get(1).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/notifications", myWarwickService.getConfigs().get(1).getNotificationPath());
    }

    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig2() {
        assertEquals(
                "Basic bW9vbndhbGtlci1hcGktdXNlcjpoYW5naW5n",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity),configs.get(1).getApiUser(),configs.get(1).getApiPassword())
                        .getHttpRequest()
                        .getHeaders()
                        .get("Authorization")
                        .get(0)
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig2() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity),configs.get(1).getApiUser(),configs.get(1).getApiPassword())
                        .getHttpRequest()
                        .getHeaders()
                        .get("content-type")
                        .get(0)
        );
    }

}
