package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.warwick.util.mywarwick.model.Configs;
import uk.ac.warwick.util.mywarwick.model.request.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickServiceImplSingleConfigTest {

    @Mock
    Configs configs;

    Config config = new Config("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");
    Activity activity = new Activity("id", "title", "url", "text", "fake-type");

    @Mock
    HttpClient httpClient;

    @InjectMocks
    private MyWarwickServiceImpl myWarwickService;

    @Before
    public void setup(){
        List<Config> configList = new ArrayList<>();
        configList.add(config);

        when(configs.getConfigs()).thenReturn(configList);

        myWarwickService.setConfig(config);
    }

    @Test
    public void httpClientShouldNotBeNull() {
        assert (myWarwickService.getHttpclient() != null);
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
    public void shouldCreateJsonBodyCorrectly() throws JsonProcessingException {
        assertEquals(
                new ObjectMapper().writeValueAsString(activity),
                myWarwickService.makeJsonBody(activity)
        );
    }

    @Test
    public void requestShouldHaveCorrectJsonBody() throws IOException {
        String expected = "{\"type\":\"fake-type\",\"title\":\"title\",\"url\":\"url\",\"tags\":[]\"recipients\":{\"users\":[\"id\"]},\"text\":\"text\"}";
        assertEquals(
                expected,
                IOUtils.toString(myWarwickService.makeRequest("", expected, "", "","").getEntity().getContent(), Charset.defaultCharset())
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
                        .makeRequest("", myWarwickService.makeJsonBody(activity), config.getApiUser(), config.getApiPassword(),"")
                        .getFirstHeader("Authorization").getValue()
        );
    }

    @Test
    public void requestShouldHaveCorrectContentType() {
        assertEquals(
                "application/json",
                myWarwickService
                        .makeRequest("", myWarwickService.makeJsonBody(activity), config.getApiUser(), config.getApiPassword(),"")
                        .getFirstHeader("content-type").getValue()
        );
    }

}
