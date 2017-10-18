package uk.ac.warwick.util.mywarwick;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.warwick.util.mywarwick.model.Configuration;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.request.Activity;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickServiceImplMultiInstanceTest {
    private String instance1BaseUrl = "https://fake.com";
    private String instance2BaseUrl = "https://ekaf.com";
    private Instance instance1 = new Instance(instance1BaseUrl, "fakeProviderId", "shylock-mywarwick-api-user", "blinking", "true");
    private Instance instance2 = new Instance(instance2BaseUrl, "fakerProviderId", "moonwalker-api-user", "hanging", "false");
    private Activity activity = new Activity("id", "title", "url", "text", "fake-type");
    private Set<Instance> instanceList = new HashSet<>();

    @Mock
    private
    HttpClient httpClient;

    @Mock
    private
    Configuration configuration;

    private MyWarwickServiceImpl myWarwickService;

    @Before
    public void setUp() {

        instanceList.add(instance1);
        instanceList.add(instance2);

        when(configuration.getInstances()).thenReturn(instanceList);

        myWarwickService = new MyWarwickServiceImpl(httpClient, configuration);
        when(httpClient.isRunning()).thenReturn(true);
    }

    private Instance getInstance1FromService() {
        return myWarwickService.getInstances().stream().filter(i -> i.getBaseUrl().equals(instance1BaseUrl)).findFirst().orElseGet(null);
    }

    private Instance getInstance2FromService() {
        return myWarwickService.getInstances().stream().filter(i -> i.getBaseUrl().equals(instance2BaseUrl)).findFirst().orElseGet(null);
    }

    private Instance getInstance1FromConfig() {
        return configuration.getInstances().stream().filter(i -> i.getBaseUrl().equals(instance1BaseUrl)).findFirst().orElseGet(null);
    }

    private Instance getInstance2FromConfig() {
        return configuration.getInstances().stream().filter(i -> i.getBaseUrl().equals(instance2BaseUrl)).findFirst().orElseGet(null);
    }

    @Test
    public void activityPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/activities", getInstance1FromService().getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/notifications", getInstance1FromService().getNotificationPath());
    }


    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig1() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), getInstance1FromConfig().getApiUser(), getInstance1FromConfig().getApiPassword(), "")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig1() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), getInstance1FromConfig().getApiUser(), getInstance1FromConfig().getApiPassword(), "")
                        .getFirstHeader("content-type").getValue()
        );
    }

    @Test
    public void activityPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/activities", getInstance2FromService().getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/notifications", getInstance2FromService().getNotificationPath());
    }

    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig2() {
        assertEquals(
                "Basic bW9vbndhbGtlci1hcGktdXNlcjpoYW5naW5n",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), getInstance2FromConfig().getApiUser(), getInstance2FromConfig().getApiPassword(), "")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig2() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), getInstance2FromConfig().getApiUser(), getInstance2FromConfig().getApiPassword(), "")
                        .getFirstHeader("content-type").getValue()
        );
    }

    @Test
    public void logErrorsShouldBeCorrectForConfig1() {
        assertEquals(true, getInstance1FromService().getLogErrors());
    }

    @Test
    public void logErrorsShouldBeCorrectForConfig2() {
        assertEquals(false, getInstance2FromService().getLogErrors());
    }

}
