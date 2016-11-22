package uk.ac.warwick.util.mywarwick.model;

public class Config {
    private String baseUrl;
    private String providerId;
    private String apiUser;
    private String apiPassword;
    private String activityPath;
    private String notificationPath;

    public Config(String baseUrl, String providerId, String apiUser, String apiPassword) {
        this.baseUrl = baseUrl;
        this.providerId = providerId;
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
        initPath();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getApiUser() {
        return apiUser;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public String getActivityPath() {
        return activityPath;
    }

    public String getNotificationPath() {
        return notificationPath;
    }

    private void initActivityPath() {
        activityPath = baseUrl + "/api/streams/" + providerId + "/activities";
    }

    private void initNotificationPath() {
        notificationPath = baseUrl + "/api/streams/" + providerId + "/notifications";
    }

    private void initPath() {
        initActivityPath();
        initNotificationPath();
    }
}
