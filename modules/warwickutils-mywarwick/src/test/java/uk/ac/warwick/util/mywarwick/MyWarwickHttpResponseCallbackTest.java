package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.response.Data;
import uk.ac.warwick.util.mywarwick.model.response.Error;
import uk.ac.warwick.util.mywarwick.model.response.Response;
import uk.ac.warwick.util.mywarwick.model.response.Warning;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MyWarwickHttpResponseCallbackTest {

    private Instance myWarwickInstance = new Instance("", "", "", "", "true");
    private Instance myWarwickInstanceNoLogErrors = new Instance("", "", "", "", "false");

    private CompletableFuture<Response> completableFuture;

    @Mock
    StatusLine fakeStatusLine;

    @Mock
    HttpResponse fakeHttpResponse;

    @Mock
    Logger fakeLogger;

    @Before
    public void setUp() {
        when(fakeStatusLine.getStatusCode()).thenReturn(200);
        completableFuture = new CompletableFuture<>();
        when(fakeHttpResponse.getStatusLine()).thenReturn(fakeStatusLine);
    }

    @After
    public void tearDown() {
        completableFuture = null;
    }

    @Test
    public void shouldHandleCompletedFullySuccessfulResponse() throws ExecutionException, InterruptedException {
        MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                "https://test.invalid/path",
                "{}",
                myWarwickInstance,
                fakeLogger,
                completableFuture,
                new AlwaysSuccessResponse()
        );
        myWarwickHttpResponseCallback.completed(fakeHttpResponse);
        assertTrue(completableFuture.isDone());
        completableFuture.get();
    }

    @Test
    public void shouldHandleCompletedSuccessfulWithWarningResponse() {
        MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                "https://test.invalid/path",
                "{}",
                myWarwickInstance,
                fakeLogger,
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
                fakeLogger,
                completableFuture,
                new AlwaysErrorResponse()
        );
        myWarwickHttpResponseCallback.completed(fakeHttpResponse);
        assertTrue(completableFuture.isDone());
        assertFalse(completableFuture.get().getErrors().isEmpty());
    }

    @Test
    public void shouldNotLogOrReturnErrors() throws ExecutionException, InterruptedException {
        MyWarwickHttpResponseCallback myWarwickHttpResponseCallback = new MyWarwickHttpResponseCallback(
                "https://test.invalid/path",
                "{}",
                myWarwickInstanceNoLogErrors,
                fakeLogger,
                completableFuture,
                new AlwaysErrorResponse()
        );
        verify(fakeLogger, never()).error(anyString(), any(Exception.class));
        verify(fakeLogger, never()).error(anyString());
        myWarwickHttpResponseCallback.completed(fakeHttpResponse);
        assertTrue(completableFuture.isDone());
        assertTrue(completableFuture.get().getErrors().isEmpty());
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
        public Response parseHttpResponseToResponseObject(HttpResponse httpResponse, ObjectMapper mapper) {
            Response response = new Response();
            response.setSuccess(false);
            response.setError(new Error("999", "this is totally wrong"));
            return response;
        }
    }


}