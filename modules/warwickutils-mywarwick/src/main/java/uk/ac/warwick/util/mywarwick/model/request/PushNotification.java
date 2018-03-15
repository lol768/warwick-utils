package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;

public class PushNotification extends Activity {

    public enum Priority {
        high, normal
    }

    private Priority priority = Priority.normal;
    private String channel;
    @JsonSerialize(using = DurationSerializer.class)
    private Duration ttl;

    public PushNotification(@NotNull String userId, @NotNull String title, String url, String text, @NotNull String type, Priority priority, String channel, Duration ttl) {
        super(userId, title, url, text, type);
        this.priority = priority;
        this.channel = channel;
        this.ttl = ttl;
    }

    public PushNotification(@NotNull Set<String> userIds, @NotNull String title, String url, String text, @NotNull String type, Priority priority, String channel, Duration ttl) {
        super(userIds, title, url, text, type);
        this.priority = priority;
        this.channel = channel;
        this.ttl = ttl;
    }

    public PushNotification(@NotNull Set<String> userIds, @NotNull Set<String> groups, @NotNull String title, String url, String text, @NotNull String type, Priority priority, String channel, Duration ttl) {
        super(userIds, groups, title, url, text, type);
        this.priority = priority;
        this.channel = channel;
        this.ttl = ttl;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
