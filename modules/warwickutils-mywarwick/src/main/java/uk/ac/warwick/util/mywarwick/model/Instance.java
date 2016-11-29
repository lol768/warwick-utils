package uk.ac.warwick.util.mywarwick.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Instance {
    private String baseUrl;
    private String providerId;
    private String apiUser;
    private String apiPassword;
    private String activityPath;
    private String notificationPath;

    public Instance(){
        super();
    }

    public Instance(String baseUrl, String providerId, String apiUser, String apiPassword) {
        this.baseUrl = baseUrl;
        this.providerId = providerId;
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
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
        if (activityPath == null) activityPath = baseUrl + "/api/streams/" + providerId + "/activities";
        return activityPath;
    }

    public String getNotificationPath() {
        if (notificationPath == null) notificationPath = baseUrl + "/api/streams/" + providerId + "/notifications";
        return notificationPath;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setApiUser(String apiUser) {
        this.apiUser = apiUser;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Instance instance = (Instance) o;

        return new EqualsBuilder()
                .append(getBaseUrl(), instance.getBaseUrl())
                .append(getProviderId(), instance.getProviderId())
                .append(getApiUser(), instance.getApiUser())
                .append(getApiPassword(), instance.getApiPassword())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBaseUrl())
                .append(getProviderId())
                .append(getApiUser())
                .append(getApiPassword())
                .toHashCode();
    }
}
