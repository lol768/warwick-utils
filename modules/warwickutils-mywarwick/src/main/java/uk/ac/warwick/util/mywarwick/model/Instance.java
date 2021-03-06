package uk.ac.warwick.util.mywarwick.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Instance {
    private String baseUrl;
    private String providerId;
    private String apiUser;
    private String apiPassword;
    private boolean logErrors = true;
    private String activityPath;
    private String notificationPath;
    private String transientPushPath;

    public Instance(){
        super();
    }

    public Instance(String baseUrl, String providerId, String apiUser, String apiPassword, String logErrors) {
        this.baseUrl = baseUrl;
        this.providerId = providerId;
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
        if (logErrors != null) {
            this.logErrors = Boolean.valueOf(logErrors);
        }
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

    public boolean getLogErrors() {
        return logErrors;
    }

    public String getActivityPath() {
        if (activityPath == null) activityPath = baseUrl + "/api/streams/" + providerId + "/activities";
        return activityPath;
    }

    public String getNotificationPath() {
        if (notificationPath == null) notificationPath = baseUrl + "/api/streams/" + providerId + "/notifications";
        return notificationPath;
    }

    public String getTransientPushPath() {
        if (transientPushPath == null) transientPushPath = baseUrl + "/api/push/" + providerId;
        return transientPushPath;
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
                .append(getLogErrors(), instance.getLogErrors())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBaseUrl())
                .append(getProviderId())
                .append(getApiUser())
                .append(getApiPassword())
                .append(getLogErrors())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("baseUrl", baseUrl)
                .append("providerId", providerId)
                .append("apiUser", apiUser)
                .append("activityPath", activityPath)
                .append("notificationPath", notificationPath)
                .append("logErrors", logErrors)
                .toString();
    }
}
