package uk.ac.warwick.util.convert.telestream;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.warwick.util.convert.ConversionStatus;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class TelestreamConversionStatus implements ConversionStatus {

    private static final DateTimeFormatter TS_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z");

    private String id;

    private String videoId;

    private Status status;

    private List<String> files;

    private List<String> screenshots;

    private ZonedDateTime created;

    private ZonedDateTime updated;

    private ZonedDateTime startedEncoding;

    private Duration encodingTime;

    private Integer progress;

    private Integer width;

    private Integer height;

    private Duration duration;

    public static TelestreamConversionStatus fromJSON(JSONObject json) throws JSONException {
        TelestreamConversionStatus status = new TelestreamConversionStatus();

        status.id = json.getString("id");
        status.videoId = json.getString("video_id");
        status.status = Status.valueOf(json.getString("status"));

        ImmutableList.Builder<String> files = ImmutableList.builder();
        JSONArray jsonFiles = json.getJSONArray("files");
        for (int i = 0; i < jsonFiles.length(); i++) {
            files.add(jsonFiles.getString(i));
        }

        status.files = files.build();

        ImmutableList.Builder<String> screenshots = ImmutableList.builder();
        JSONArray jsonScreenshots = json.getJSONArray("screenshots");
        for (int i = 0; i < jsonScreenshots.length(); i++) {
            screenshots.add(jsonScreenshots.getString(i));
        }

        status.screenshots = screenshots.build();

        status.created = ZonedDateTime.parse(json.getString("created_at"), TS_DATETIME_FORMAT);
        status.updated = ZonedDateTime.parse(json.getString("updated_at"), TS_DATETIME_FORMAT);
        status.startedEncoding = json.isNull("started_encoding_at") ? null : ZonedDateTime.parse(json.getString("started_encoding_at"), TS_DATETIME_FORMAT);
        status.encodingTime = json.isNull("encoding_time") ? null : Duration.ofSeconds(json.getLong("encoding_time"));
        status.progress = json.isNull("encoding_progress") ? null : json.getInt("encoding_progress");
        status.width = json.isNull("width") ? null : json.getInt("width");
        status.height = json.isNull("height") ? null : json.getInt("height");
        status.duration = json.isNull("duration") ? null : Duration.ofMillis(json.getLong("duration"));

        return status;
    }

    public String getId() {
        return id;
    }

    public String getVideoId() {
        return videoId;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getFiles() {
        return files;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public ZonedDateTime getStartedEncoding() {
        return startedEncoding;
    }

    public Duration getEncodingTime() {
        return encodingTime;
    }

    public Integer getProgress() {
        return progress;
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

        TelestreamConversionStatus that = (TelestreamConversionStatus) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(videoId, that.videoId)
            .append(status, that.status)
            .append(files, that.files)
            .append(screenshots, that.screenshots)
            .append(created, that.created)
            .append(updated, that.updated)
            .append(startedEncoding, that.startedEncoding)
            .append(encodingTime, that.encodingTime)
            .append(progress, that.progress)
            .append(width, that.width)
            .append(height, that.height)
            .append(duration, that.duration)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(videoId)
            .append(status)
            .append(files)
            .append(screenshots)
            .append(created)
            .append(updated)
            .append(startedEncoding)
            .append(encodingTime)
            .append(progress)
            .append(width)
            .append(height)
            .append(duration)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("id", id)
            .append("videoId", videoId)
            .append("status", status)
            .append("files", files)
            .append("screenshots", screenshots)
            .append("created", created)
            .append("updated", updated)
            .append("startedEncoding", startedEncoding)
            .append("encodingTime", encodingTime)
            .append("progress", progress)
            .append("width", width)
            .append("height", height)
            .append("duration", duration)
            .toString();
    }
}
