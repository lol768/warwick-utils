package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.ac.warwick.util.mywarwick.model.Instance;

import static org.junit.Assert.*;

import uk.ac.warwick.util.mywarwick.model.response.Data;
import uk.ac.warwick.util.mywarwick.model.response.Error;
import uk.ac.warwick.util.mywarwick.model.response.Response;
import uk.ac.warwick.util.mywarwick.model.response.Warning;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickHttpResponseCallbackTest {

    Instance myWarwickInstance = new Instance("", "", "", "", "true");
    Logger testLogger = TestLoggerFactory.getTestLogger("uk.ac.warwick.util.core.LoggerTest");

    CompletableFuture<Response> completableFuture;

    @Mock
    StatusLine fakeStatusLine;

    @Mock
    HttpResponse fakeHttpResponse;

    @Before
    public void setUp() throws Exception {
        when(fakeStatusLine.getStatusCode()).thenReturn(200);
        completableFuture = new CompletableFuture<>();
        when(fakeHttpResponse.getStatusLine()).thenReturn(fakeStatusLine);
    }

    @After
    public void tearDown() throws Exception {
        completableFuture = null;
    }

    @Test
    public void shouldHandleCompletedFullySuccessfulResponse() throws ExecutionException, InterruptedException {

        MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                "https://test.invalid/path",
                "{}",
                myWarwickInstance,
                testLogger,
                completableFuture,
                new AlwaysSuccessResponse()
        );
        myWarwickHttpResponseCallback.completed(fakeHttpResponse);
        assertTrue(completableFuture.isDone());
        Response response = completableFuture.get();
    }

    @Test
    public void shouldHandleCompletedSuccessfulWithWarningResponse() throws ExecutionException, InterruptedException {
        MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                "https://test.invalid/path",
                "{}",
                myWarwickInstance,
                testLogger,
                completableFuture,
                new AlwaysSuccessWithWarningResponse()
        );
        myWarwickHttpResponseCallback.completed(fakeHttpResponse);
        assertTrue(completableFuture.isDone());
    }

    @Test
    public void shouldHandleCompletedWithErrorsResponse() throws ExecutionException, InterruptedException {
        MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                "https://test.invalid/path",
                "{}",
                myWarwickInstance,
                testLogger,
                completableFuture,
                new AlwaysErrorResponse()
        );
        myWarwickHttpResponseCallback.completed(fakeHttpResponse);
        assertTrue(completableFuture.isDone());
    }

    class AlwaysSuccessResponse implements MyWarwickHttpResponseCallbackHelper {
        @Override
        public Response parseHttpResponseToResponseObject(HttpResponse httpResponse, ObjectMapper mapper) throws IOException {
            Response response = new Response();
            response.setSuccess(true);
            response.setData(new Data("123"));
            return response;
        }

    }

    class AlwaysSuccessWithWarningResponse extends AlwaysSuccessResponse {
        @Override
        public Response parseHttpResponseToResponseObject(HttpResponse httpResponse, ObjectMapper mapper) throws IOException {
            Response response = super.parseHttpResponseToResponseObject(httpResponse, mapper);
            response.setWarnings(Collections.singletonList(new Warning("InvalidUsercodeAudience", "The request contains one or more invalid usercode: List()")));
            return response;
        }
    }

    class AlwaysErrorResponse implements MyWarwickHttpResponseCallbackHelper {
        @Override
        public Response parseHttpResponseToResponseObject(HttpResponse httpResponse, ObjectMapper mapper) throws IOException {
            Response response = new Response();
            response.setSuccess(false);
            response.setError(new Error("999", "this is totally wrong"));
            return response;
        }
    }


}