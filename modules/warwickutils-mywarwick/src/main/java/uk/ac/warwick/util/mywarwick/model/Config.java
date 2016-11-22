package uk.ac.warwick.util.mywarwick.model;

public class Config {
    String baseUrl;
    String providerId;
    String apiUser;
    String apiPassword;

    public Config(String baseUrl, String providerId, String apiUser, String apiPassword) {
        this.baseUrl = baseUrl;
        this.providerId = providerId;
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getApiUser() {
        return apiUser;
    }

    public void setApiUser(String apiUser) {
        this.apiUser = apiUser;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }
}
