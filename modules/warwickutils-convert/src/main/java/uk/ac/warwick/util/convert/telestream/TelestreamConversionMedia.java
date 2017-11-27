package uk.ac.warwick.util.convert.telestream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.warwick.util.convert.ConversionMedia;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class TelestreamConversionMedia implements ConversionMedia {

    private static final DateTimeFormatter TS_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z");

    private String id;

    private String originalFilename;

    private ZonedDateTime created;

    private ZonedDateTime updated;

    private Status status;

    private Integer width;

    private Integer height;

    private Duration duration;

    public static TelestreamConversionMedia fromJSON(JSONObject json) throws JSONException {
        TelestreamConversionMedia media = new TelestreamConversionMedia();

        media.id = json.getString("id");
        media.originalFilename = json.getString("original_filename");
        media.created = ZonedDateTime.parse(json.getString("created_at"), TS_DATETIME_FORMAT);
        media.updated = ZonedDateTime.parse(json.getString("updated_at"), TS_DATETIME_FORMAT);
        media.status = Status.valueOf(json.getString("status"));
        media.width = json.isNull("width") ? null : json.getInt("width");
        media.height = json.isNull("height") ? null : json.getInt("height");
        media.duration = json.isNull("duration") ? null : Duration.ofMillis(json.getLong("duration"));

        return media;
    }

    public String getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getUpdated() {
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

        TelestreamConversionMedia that = (TelestreamConversionMedia) o;

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
