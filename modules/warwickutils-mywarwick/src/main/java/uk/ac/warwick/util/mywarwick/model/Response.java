package uk.ac.warwick.util.mywarwick.model;

public class Response {
    private Activity activity;
    private String requestPath;
    private Integer responseCode;

    public Response(Activity activity, String requestPath, Integer responseCode) {
        this.activity = activity;
        this.requestPath = requestPath;
        this.responseCode = responseCode;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public Integer getResponseCode() {
        return responseCode;
    }
}
