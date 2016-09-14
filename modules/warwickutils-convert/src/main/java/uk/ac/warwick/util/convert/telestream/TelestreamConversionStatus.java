package uk.ac.warwick.util.convert.telestream;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.warwick.util.convert.ConversionStatus;

import java.util.List;

class TelestreamConversionStatus implements ConversionStatus {

    private static final DateTimeFormatter TS_DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss Z");

    private String id;

    private String videoId;

    private Status status;

    private List<String> files;

    private List<String> screenshots;

    private DateTime created;

    private DateTime updated;

    private DateTime startedEncoding;

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

        status.created = TS_DATETIME_FORMAT.parseDateTime(json.getString("created_at"));
        status.updated = TS_DATETIME_FORMAT.parseDateTime(json.getString("updated_at"));
        status.startedEncoding = json.isNull("started_encoding_at") ? null : TS_DATETIME_FORMAT.parseDateTime(json.getString("started_encoding_at"));
        status.encodingTime = json.isNull("encoding_time") ? null : Duration.standardSeconds(json.getLong("encoding_time"));
        status.progress = json.isNull("encoding_progress") ? null : json.getInt("encoding_progress");
        status.width = json.isNull("width") ? null : json.getInt("width");
        status.height = json.isNull("height") ? null : json.getInt("height");
        status.duration = json.isNull("duration") ? null : Duration.millis(json.getLong("duration"));


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

    public DateTime getCreated() {
        return created;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public DateTime getStartedEncoding() {
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
