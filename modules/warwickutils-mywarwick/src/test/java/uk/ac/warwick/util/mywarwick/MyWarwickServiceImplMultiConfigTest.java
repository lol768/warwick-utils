package uk.ac.warwick.util.mywarwick;

import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.AsyncHttpClient;
import uk.ac.warwick.util.mywarwick.MyWarwickServiceImpl;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MyWarwickServiceImplMultiConfigTest {
    List<Config> configs;
    MyWarwickServiceImpl myWarwickService;
    Activity activity;
    AsyncHttpClient asyncHttpClient;

    @Before
    public void setUp() {
        Config config1 = new Config("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");
        Config config2 = new Config("https://ekaf.com", "fakerProviderId", "moonwalker-api-user", "hanging");
        configs = new ArrayList<>();
        configs.add(config1);
        configs.add(config2);
        asyncHttpClient = new AsyncHttpClient();
        myWarwickService = new MyWarwickServiceImpl(asyncHttpClient, configs);
        activity = new Activity("id", "title", "url", "text", "fake-type");
    }

    @Test
    public void activityPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/activities", myWarwickService.getConfigs().get(1).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig1() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/notifications", myWarwickService.getConfigs().get(1).getNotificationPath());
    }


    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig1() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configs.get(0).getApiUser(), configs.get(0).getApiPassword(), "")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig1() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configs.get(0).getApiUser(), configs.get(0).getApiPassword(), "")
                        .getFirstHeader("content-type").getValue()
        );
    }

    @Test
    public void activityPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/activities", myWarwickService.getConfigs().get(0).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrectForConfig2() {
        assertEquals("https://ekaf.com/api/streams/fakerProviderId/notifications", myWarwickService.getConfigs().get(0).getNotificationPath());
    }

    @Test
    public void requestShouldHaveCorrectAuthHeaderForConfig2() {
        assertEquals(
                "Basic bW9vbndhbGtlci1hcGktdXNlcjpoYW5naW5n",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configs.get(1).getApiUser(), configs.get(1).getApiPassword(), "")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentTypeForConfig2() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), configs.get(1).getApiUser(), configs.get(0).getApiPassword(), "")
                        .getFirstHeader("content-type").getValue()
        );
    }

    @Test
    public void configsShouldNotHaveDuplicate() {

        Config config2copy = new Config("https://ekaf.com", "fakerProviderId", "moonwalker-api-user", "hanging");
        ArrayList<Config> configArrayList = new ArrayList<>();
        configArrayList.addAll(configs);
        configArrayList.add(config2copy);
        MyWarwickServiceImpl myWarwickService = new MyWarwickServiceImpl(asyncHttpClient, configArrayList);
        assertEquals(2, myWarwickService.getConfigs().size());
    }

}
