package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.response.Error;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MyWarwickHttpResponseCallback implements FutureCallback<HttpResponse> {

    private final String reqPath;
    private final String reqJson;
    private final Instance myWarwickInstance;
    private final Logger logger;
    private final ObjectMapper mapper;
    private final CompletableFuture<Response> completableFuture;
    private Response response;
    private MyWarwickHttpResponseCallbackHelper myWarwickHttpResponseCallbackHelper;

    public MyWarwickHttpResponseCallback(
            @NotNull String reqPath,
            @NotNull String reqJson,
            @NotNull Instance myWarwickInstance,
            @NotNull Logger logger,
            @NotNull CompletableFuture<Response> completableFuture,
            @NotNull MyWarwickHttpResponseCallbackHelper myWarwickHttpResponseCallbackHelper
    ) {
        super();
        this.reqPath = reqPath;
        this.reqJson = reqJson;
        this.myWarwickInstance = myWarwickInstance;
        this.logger = logger;
        this.completableFuture = completableFuture;
        this.mapper = new ObjectMapper();
        this.response = new Response();
        this.myWarwickHttpResponseCallbackHelper = myWarwickHttpResponseCallbackHelper;
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        if (logger.isDebugEnabled()) logger.debug("Request completed");
        try {
            response = myWarwickHttpResponseCallbackHelper.parseHttpResponseToResponseObject(httpResponse, mapper);
            completableFuture.complete(response);
            if (response.getErrors().size() != 0) {
                logError(myWarwickInstance, "Request completed but it contains error(s):" +
                        "\nbaseUrl:" + myWarwickInstance.getBaseUrl() +
                        "\nHTTP Status Code: " + httpResponse.getStatusLine().getStatusCode() +
                        "\nResponse:\n" + response.toString()
                );
            }
            if (response.getWarnings().size() != 0) {
                logger.warn("Request completed but it contains warning(s):" +
                        "\nbaseUrl:" + myWarwickInstance.getBaseUrl() +
                        "\nHTTP Status Code: " + httpResponse.getStatusLine().getStatusCode() +
                        "\nResponse:\n" + response.toString()
                );
            }
        } catch (IOException e) {
            logError(myWarwickInstance, "An IOException was thrown communicating with mywarwick:\n" +
                    e.getMessage() +
                    "\nbaseUrl: " + myWarwickInstance.getBaseUrl());
            response.setError(new Error("", e.getMessage()));
            completableFuture.complete(response);
        }
    }

    @Override
    public void failed(Exception e) {
        logError(myWarwickInstance, "Request to mywarwick API has failed with errors:" +
                "\npath: " + reqPath +
                "\ninstance: " + myWarwickInstance +
                "\nrequest json " + reqJson +
                "\nerror message:" + e.getMessage(), e);
        response.setError(new Error("", e.getMessage()));
        completableFuture.complete(response);
    }

    @Override
    public void cancelled() {
        String message = "Request to mywarwick has been cancelled";
        if (logger.isDebugEnabled()) logger.debug(message);
        response.setError(new Error("", message));
        completableFuture.complete(response);
    }

    void logError(Instance instance, String message) {
        if (instance.getLogErrors()) {
            logger.error(message);
        } else {
            logger.warn(message);
        }
    }

    void logError(Instance instance, String message, Exception e) {
        if (instance.getLogErrors()) {
            logger.error(message, e);
        } else {
            logger.warn(message, e);
        }
    }

}
