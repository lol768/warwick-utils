package uk.ac.warwick.util.mywarwick.model.request;

// activity and notification share the same data model, they are only different

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class Activity implements ValidActivity {
    private String type;
    private String title;
    private String text;
    private String url;
    private Set<Tag> tags;
    private Recipients recipients;
    private Boolean sendEmail;

    public Activity() {
        this.recipients = new Recipients();
        this.tags = new HashSet<>();
    }

    public Activity(@NotNull String userId, @NotNull String title, String url, String text, @NotNull String type) {
        this.recipients = new Recipients(userId);
        this.title = title;
        this.text = text;
        this.url = url;
        this.type = type;
    }

    public Activity(@NotNull Set<String> userIds, @NotNull String title, String url, String text, @NotNull String type) {
        this.recipients = new Recipients(userIds);
        this.title = title;
        this.text = text;
        this.url = url;
        this.type = type;
    }

    public Activity(@NotNull Set<String> userIds, @NotNull Set<String> groups, @NotNull String title, String url, String text, @NotNull String type) {
        this.recipients = new Recipients(userIds, groups);
        this.title = title;
        this.text = text;
        this.url = url;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Activity activity = (Activity) o;

        return new EqualsBuilder()
                .append(getType(), activity.getType())
                .append(getTitle(), activity.getTitle())
                .append(getUrl(), activity.getUrl())
                .append(getRecipients(), activity.getRecipients())
                .append(getText(), activity.getText())
                .append(getSendEmail(), activity.getSendEmail())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getType())
                .append(getTitle())
                .append(getUrl())
                .append(getRecipients())
                .append(getText())
                .append(getSendEmail())
                .toHashCode();
    }

    public String getType() {
        return type;
    }

    public void setType(@NotNull String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(@NotNull String url) {
        this.url = url;
    }

    public Recipients getRecipients() {
        return recipients;
    }

    public void setRecipients(@NotNull Recipients recipients) {
        this.recipients = recipients;
    }

    public String getText() {
        return text;
    }

    public void setText(@NotNull String text) {
        this.text = text;
    }

    public Set<Tag> getTags() {
        if (tags == null) tags = new HashSet<>();
        return tags;
    }

    public void setTags(@NotNull Set<Tag> tags) {
        this.tags = tags;
    }

    public void setTags(@NotNull Tag tag) {
        getTags().add(tag);
    }

    @JsonProperty("send_email")
    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
}
