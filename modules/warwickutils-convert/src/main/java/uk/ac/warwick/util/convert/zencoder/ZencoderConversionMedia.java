package uk.ac.warwick.util.convert.zencoder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.warwick.util.convert.ConversionMedia;

class ZencoderConversionMedia implements ConversionMedia {

    private static final DateTimeFormatter DATETIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

    private int id;

    private String s3Url;

    private DateTime created;

    private DateTime updated;

    private Status status;

    private Integer width;

    private Integer height;

    private Duration duration;

    public static ZencoderConversionMedia fromJobJSON(JSONObject json) throws JSONException {
        ZencoderConversionMedia media = new ZencoderConversionMedia();

        media.id = json.getInt("id");
        media.created = DATETIME_FORMAT.parseDateTime(json.getString("created_at"));
        media.updated = DATETIME_FORMAT.parseDateTime(json.getString("updated_at"));

        JSONObject input = json.getJSONObject("input_media_file");

        media.s3Url = input.getString("url");
        switch (input.getString("state")) {
            case "assigning":
            case "pending":
            case "queued":
            case "waiting":
            case "processing":
                media.status = Status.processing;
                break;
            case "finished":
                media.status = Status.success;
                break;
            case "failed":
            case "cancelled":
                media.status = Status.fail;
                break;
            default:
                throw new IllegalStateException("Unexpected input state: " + input.getString("state"));
        }
        media.width = input.isNull("width") ? null : input.getInt("width");
        media.height = input.isNull("height") ? null : input.getInt("height");
        media.duration = input.isNull("duration_in_ms") ? null : Duration.millis(input.getLong("duration_in_ms"));

        return media;
    }

    public String getId() {
        return Integer.toString(id);
    }

    public String getOriginalFilename() {
        return s3Url;
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

        ZencoderConversionMedia that = (ZencoderConversionMedia) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(s3Url, that.s3Url)
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
            .append(s3Url)
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
            .append("s3Url", s3Url)
            .append("created", created)
            .append("updated", updated)
            .append("status", status)
            .append("width", width)
            .append("height", height)
            .append("duration", duration)
            .toString();
    }
}
