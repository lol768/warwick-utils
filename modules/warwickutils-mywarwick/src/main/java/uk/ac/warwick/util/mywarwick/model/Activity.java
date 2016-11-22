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
