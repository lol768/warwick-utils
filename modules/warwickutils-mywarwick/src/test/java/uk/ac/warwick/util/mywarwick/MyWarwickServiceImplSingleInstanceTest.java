package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.warwick.util.mywarwick.model.Configuration;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.request.Activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickServiceImplSingleInstanceTest {

    @Mock
    Configuration configuration;

    Instance instance = new Instance("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking", "true");
    Activity activity = new Activity("id", "title", "url", "text", "fake-type");

    @Mock
    HttpClient httpClient;

    private MyWarwickServiceImpl myWarwickService;

    @Before
    public void setup() {
        Set<Instance> instanceList = new HashSet<>();
        instanceList.add(instance);

        when(configuration.getInstances()).thenReturn(instanceList);

        myWarwickService = new MyWarwickServiceImpl(httpClient, configuration);
    }

    @Test
    public void httpClientShouldNotBeNull() {
        assert (myWarwickService.getHttpClient() != null);
    }

    @Test
    public void activityPathShouldBeCorrect() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/activities", myWarwickService.getInstances().stream().collect(Collectors.toList()).get(0).getActivityPath());
    }

    @Test
    public void notificationPathShouldBeCorrect() {
        assertEquals("https://fake.com/api/streams/fakeProviderId/notifications", myWarwickService.getInstances().stream().collect(Collectors.toList()).get(0).getNotificationPath());
    }

    @Test
    public void shouldCreateJsonBodyCorrectly() throws JsonProcessingException {
        assertEquals(
                new ObjectMapper().writeValueAsString(activity),
                myWarwickService.makeJsonBody(activity)
        );
    }

    @Test
    public void requestShouldSerializeSendEmailCorrectly() throws IOException {
        Activity nullSendEmail = activity;
        JsonNode nullSendEmailJson = new ObjectMapper().readTree(myWarwickService.makeJsonBody(nullSendEmail));
        assertTrue(nullSendEmailJson.path("send_email").isNull());

        Activity falseSendEmail = new Activity("id", "title", "url", "text", "fake-type");
        falseSendEmail.setSendEmail(false);
        JsonNode falseSendEmailJson = new ObjectMapper().readTree(myWarwickService.makeJsonBody(falseSendEmail));
        assertTrue(falseSendEmailJson.path("send_email").isBoolean());
        assertFalse(falseSendEmailJson.path("send_email").asBoolean());

        Activity trueSendEmail = new Activity("id", "title", "url", "text", "fake-type");
        trueSendEmail.setSendEmail(true);
        JsonNode trueSendEmailJson = new ObjectMapper().readTree(myWarwickService.makeJsonBody(trueSendEmail));
        assertTrue(trueSendEmailJson.path("send_email").isBoolean());
        assertTrue(trueSendEmailJson.path("send_email").asBoolean());
    }

    @Test
    public void requestShouldHaveCorrectJsonBody() throws IOException {
        String expected = "{\"type\":\"fake-type\",\"title\":\"title\",\"url\":\"url\",\"tags\":[]\"recipients\":{\"users\":[\"id\"]},\"text\":\"text\"}";
        assertEquals(
                expected,
                IOUtils.toString(myWarwickService.makeRequest("", expected, "", "","").getEntity().getContent(), StandardCharsets.UTF_8)
        );
    }

    @Test
    public void requestShouldHaveCorrectPath() throws MalformedURLException {
        String expected = "http://test.com";
        assertEquals(
                expected,
                myWarwickService.makeRequest(expected, "", "", "","").getURI().toURL().toString()
        );
    }

    @Test
    public void requestShouldHaveCorrectAuthHeader() {
        assertEquals(
                "Basic c2h5bG9jay1teXdhcndpY2stYXBpLXVzZXI6Ymxpbmtpbmc=",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), instance.getApiUser(), instance.getApiPassword(),"")
                        .getFirstHeader("Authorization").getValue()
        );
    }

}
