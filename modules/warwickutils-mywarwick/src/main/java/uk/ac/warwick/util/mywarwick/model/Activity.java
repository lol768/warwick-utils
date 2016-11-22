package uk.ac.warwick.util.mywarwick.model;

// activity and notification share the same data model, they are only different

public class Activity {
    String type;
    String title;
    String url;
    Recipients recipients;
    String text;

    public Activity(String userId, String title, String url, String text, String type) {
        this.recipients = new Recipients(userId);
        this.title = title;
        this.text = text;
        this.url = url;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity)) return false;

        Activity activity = (Activity) o;

        if (!getType().equals(activity.getType())) return false;
        if (!getTitle().equals(activity.getTitle())) return false;
        if (getUrl() != null ? !getUrl().equals(activity.getUrl()) : activity.getUrl() != null) return false;
        if (!getRecipients().equals(activity.getRecipients())) return false;
        return getText() != null ? getText().equals(activity.getText()) : activity.getText() == null;

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getTitle().hashCode();
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + getRecipients().hashCode();
        result = 31 * result + (getText() != null ? getText().hashCode() : 0);
        return result;
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
}
