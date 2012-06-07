package uk.ac.warwick.util.core.lookup;

import java.io.Serializable;

import org.apache.http.Header;

import com.google.common.collect.ImmutableSet;

public final class TwitterTimelineResponse implements Serializable {
    
    private static final long serialVersionUID = -7577232383969025803L;
    
    private final String responseBody;
    private final int statusCode;
    private final ImmutableSet<Header> headers;
    
    public TwitterTimelineResponse(String b, int status, Header[] theHeaders) {
        this.responseBody = b;
        this.statusCode = status;
        this.headers = ImmutableSet.copyOf(theHeaders);
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ImmutableSet<Header> getHeaders() {
        return headers;
    }

}
