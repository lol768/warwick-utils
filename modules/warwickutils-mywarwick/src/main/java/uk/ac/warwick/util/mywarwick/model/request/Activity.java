package uk.ac.warwick.util.mywarwick.model.request;

// activity and notification share the same data model, they are only different

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class Activity {
    private String type;
    private String title;
    private String text;
    private String url;
    private Set<Tag> tags;
    private Recipients recipients;

    public Activity(String userId, String title, String url, String text, String type) {
        this.recipients = new Recipients(userId);
        this.title = title;
        this.text = text;
        this.url = url;
        this.type = type;
    }

    public Activity(Set<String> userIds, String title, String url, String text, String type) {
        this.recipients = new Recipients(userIds);
        this.title = title;
        this.text = text;
        this.url = url;
        this.type = type;
    }

    public Activity(Set<String> userIds, Set<String> groups, String title, String url, String text, String type) {
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
                .toHashCode();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Recipients getRecipients() {
        return recipients;
    }

    public void setRecipients(Recipients recipients) {
        this.recipients = recipients;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<Tag> getTags() {
        if (tags == null) tags = new HashSet<>();
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void setTags(Tag tag) {
        getTags().add(tag);
    }
}
