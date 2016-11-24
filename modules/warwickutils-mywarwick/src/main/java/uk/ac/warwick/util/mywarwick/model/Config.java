package uk.ac.warwick.util.mywarwick.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.inject.Named;

@Named
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        return new EqualsBuilder()
                .append(getBaseUrl(), config.getBaseUrl())
                .append(getProviderId(), config.getProviderId())
                .append(getApiUser(), config.getApiUser())
                .append(getApiPassword(), config.getApiPassword())
                .append(getActivityPath(), config.getActivityPath())
                .append(getNotificationPath(), config.getNotificationPath())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBaseUrl())
                .append(getProviderId())
                .append(getApiUser())
                .append(getApiPassword())
                .append(getActivityPath())
                .append(getNotificationPath())
                .toHashCode();
    }

}
