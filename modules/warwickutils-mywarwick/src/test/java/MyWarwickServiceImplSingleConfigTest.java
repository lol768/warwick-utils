import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.AsyncHttpClient;
import uk.ac.warwick.util.mywarwick.MyWarwickServiceImpl;
import uk.ac.warwick.util.mywarwick.model.Activity;
import uk.ac.warwick.util.mywarwick.model.Config;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import static org.junit.Assert.assertEquals;

public class MyWarwickServiceImplSingleConfigTest {

    private Config config = new Config("https://fake.com", "fakeProviderId", "shylock-mywarwick-api-user", "blinking");

    private Activity activity = new Activity("id", "title", "url", "text", "fake-type");

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private MyWarwickServiceImpl myWarwickService = new MyWarwickServiceImpl(asyncHttpClient, config);

    @Test
    public void httpClientShouldNotBeNull() {
        assert (myWarwickService.getHttpclient() != null);
    }

    @Test
    public void httpClientShouldBeStarted() {
        assertEquals(true, myWarwickService.getHttpclient().isRunning());
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
    public void shouldCreateIdenticalJsonBodyAgainstAnotherJsonLibrary() {
        assertEquals(
                new Gson().toJson(activity),
                myWarwickService.makeJsonBody(activity)
        );
    }

    @Test
    public void requestShouldHaveCorrectJsonBody() throws IOException {
        String expected = "{\"type\":\"fake-type\",\"title\":\"title\",\"url\":\"url\",\"recipients\":{\"users\":[\"id\"]},\"text\":\"text\"}";
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
