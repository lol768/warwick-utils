package uk.ac.warwick.util.mywarwick;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.response.Error;
import uk.ac.warwick.util.mywarwick.model.response.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MyWarwickHttpResponseCallback implements FutureCallback<HttpResponse> {

    final String reqPath;
    final String reqJson;
    final Instance myWarwickInstance;
    final Logger logger;
    final ObjectMapper mapper = new ObjectMapper();
    final CompletableFuture<Response> completableFuture;

    private MyWarwickHttpResponseCallback(
            String reqPath,
            String reqJson,
            Instance myWarwickInstance,
            Logger logger,
            CompletableFuture<Response> completableFuture
    ) {
        super();
        this.reqPath = reqPath;
        this.reqJson = reqJson;
        this.myWarwickInstance = myWarwickInstance;
        this.logger = logger;
        this.completableFuture = completableFuture;
    }

    static MyWarwickHttpResponseCallback newInstance(
            String reqPath,
            String reqJson,
            Instance myWarwickInstance,
            Logger logger,
            CompletableFuture<Response> completableFuture
    ) {
        return new MyWarwickHttpResponseCallback(reqPath, reqJson, myWarwickInstance, logger, completableFuture);
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        if (logger.isDebugEnabled()) logger.debug("Request completed");
        try {
            Response response = parseHttpResponseToResponseObject(httpResponse);
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
            Response errorResponse = new Response();
            logError(myWarwickInstance, "An IOException was thrown communicating with mywarwick:\n" +
                    e.getMessage() +
                    "\nbaseUrl: " + myWarwickInstance.getBaseUrl());
            errorResponse.setError(new Error("", e.getMessage()));
            completableFuture.complete(errorResponse);
        }
    }

    @Override
    public void failed(Exception e) {
        logError(myWarwickInstance, "Request to mywarwick API has failed with errors:" +
                "\npath: " + reqPath +
                "\ninstance: " + myWarwickInstance +
                "\nrequest json " + reqJson +
                "\nerror message:" + e.getMessage(), e);
        Response failedResponse = new Response();
        failedResponse.setError(new Error("", e.getMessage()));
        completableFuture.complete(failedResponse);
    }

    @Override
    public void cancelled() {
        String message = "Request to mywarwick has been cancelled";
        if (logger.isDebugEnabled()) logger.debug(message);
        Response cancelledResponse = new Response();
        cancelledResponse.setError(new Error("", message));
        completableFuture.complete(cancelledResponse);
    }


    public Response parseHttpResponseToResponseObject(HttpResponse httpResponse) throws IOException {
        String responseString = EntityUtils.toString(httpResponse.getEntity());
        return mapper.readValue(responseString, Response.class);
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
