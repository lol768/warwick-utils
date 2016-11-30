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
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickServiceImplMultiInstanceTest {
    Instance instance1 = new Instance("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");
    Instance instance2 = new Instance("https://ekaf.com", "fakerProviderId", "moonwalker-api-user", "hanging");
    Activity activity = new Activity("id", "title", "url", "text", "fake-type");
    Set<Instance> instanceList = new HashSet<>();

    @Mock
    HttpClient httpClient;

    @Mock
    Configuration configuration;

    MyWarwickServiceImpl myWarwickService;

    @Before
    public void setUp() {

        instanceList.add(instance1);
        instanceList.add(instance2);

        when(configuration.getInstances()).thenReturn(instanceList);

        myWarwickService = new MyWarwickServiceImpl(httpClient, configuration);
        when(httpClient.isRunning()).thenReturn(true);
    }

    @Test
    public void activityPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/activities", myWarwickService.getInstances().stream().collect(Collectors.toList()).get(0).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/notifications", myWarwickService.getInstances().stream().collect(Collectors.toList()).get(0).getNotificationPath());
    }


    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig1() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configuration.getInstances().stream().collect(Collectors.toList()).get(0).getApiUser(), configuration.getInstances().stream().collect(Collectors.toList()).get(0).getApiPassword(), "")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig1() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configuration.getInstances().stream().collect(Collectors.toList()).get(0).getApiUser(), configuration.getInstances().stream().collect(Collectors.toList()).get(0).getApiPassword(), "")
                        .getFirstHeader("content-type").getValue()
        );
    }

    @Test
    public void activityPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/activities", myWarwickService.getInstances().stream().collect(Collectors.toList()).get(1).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/notifications", myWarwickService.getInstances().stream().collect(Collectors.toList()).get(1).getNotificationPath());
    }

    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig2() {
        assertEquals(
                "Basic bW9vbndhbGtlci1hcGktdXNlcjpoYW5naW5n",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configuration.getInstances().stream().collect(Collectors.toList()).get(1).getApiUser(), configuration.getInstances().stream().collect(Collectors.toList()).get(1).getApiPassword(), "")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig2() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configuration.getInstances().stream().collect(Collectors.toList()).get(1).getApiUser(), configuration.getInstances().stream().collect(Collectors.toList()).get(0).getApiPassword(), "")
                        .getFirstHeader("content-type").getValue()
        );
    }



}
