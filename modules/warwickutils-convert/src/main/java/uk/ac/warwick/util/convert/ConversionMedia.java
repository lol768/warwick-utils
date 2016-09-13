package uk.ac.warwick.util.convert;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public class ConversionMedia {

    private static final DateTimeFormatter TS_DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss Z");

    public enum Status {
        success, fail, processing
    }

    private String id;

    private String originalFilename;

    private DateTime created;

    private DateTime updated;

    private Status status;

    private Integer width;

    private Integer height;

    private Duration duration;

    public static ConversionMedia fromJSON(JSONObject json) throws JSONException {
        ConversionMedia media = new ConversionMedia();

        media.id = json.getString("id");
        media.originalFilename = json.getString("original_filename");
        media.created = TS_DATETIME_FORMAT.parseDateTime(json.getString("created_at"));
        media.updated = TS_DATETIME_FORMAT.parseDateTime(json.getString("updated_at"));
        media.status = ConversionMedia.Status.valueOf(json.getString("status"));
        media.width = json.isNull("width") ? null : json.getInt("width");
        media.height = json.isNull("height") ? null : json.getInt("height");
        media.duration = json.isNull("duration") ? null : Duration.millis(json.getLong("duration"));

        return media;
    }

    public String getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public DateTime getCreated() {
        return created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public Status getStatus() {
        return status;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ConversionMedia that = (ConversionMedia) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(originalFilename, that.originalFilename)
            .append(created, that.created)
            .append(updated, that.updated)
            .append(status, that.status)
            .append(width, that.width)
            .append(height, that.height)
            .append(duration, that.duration)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(originalFilename)
            .append(created)
            .append(updated)
            .append(status)
            .append(width)
            .append(height)
            .append(duration)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("id", id)
            .append("originalFilename", originalFilename)
            .append("created", created)
            .append("updated", updated)
            .append("status", status)
            .append("width", width)
            .append("height", height)
            .append("duration", duration)
            .toString();
    }
}
