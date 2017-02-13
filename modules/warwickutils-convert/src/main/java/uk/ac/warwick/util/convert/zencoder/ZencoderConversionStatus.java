package uk.ac.warwick.util.convert.zencoder;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.warwick.util.convert.ConversionStatus;
import uk.ac.warwick.util.web.Uri;

import java.util.Collections;
import java.util.List;

class ZencoderConversionStatus implements ConversionStatus {

    private static final DateTimeFormatter DATETIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

    private int id;

    private Status status;

    private List<String> files;

    private List<String> screenshots;

    private DateTime created;

    private DateTime updated;

    private Integer progress;

    public static ZencoderConversionStatus fromProgressJSON(int id, JSONObject json) throws JSONException {
        ZencoderConversionStatus status = new ZencoderConversionStatus();

        status.id = id;
        switch (json.getString("state")) {
            case "assigning":
            case "pending":
            case "queued":
            case "waiting":
            case "processing":
                status.status = Status.processing;
                break;
            case "finished":
                status.status = Status.success;
                break;
            case "failed":
            case "cancelled":
                status.status = Status.fail;
                break;
            default:
                throw new IllegalStateException("Unexpected input state: " + json.getString("state"));
        }

        status.files = Collections.emptyList();
        status.screenshots = Collections.emptyList();

        status.progress = json.isNull("progress") ? null : (int) Math.ceil(json.getDouble("progress"));

        return status;
    }

    public static ZencoderConversionStatus fromCompletedJobJSON(JSONObject json) throws JSONException {
        ZencoderConversionStatus status = new ZencoderConversionStatus();

        status.id = json.getInt("id");
        status.created = DATETIME_FORMAT.parseDateTime(json.getString("created_at"));
        status.updated = DATETIME_FORMAT.parseDateTime(json.getString("updated_at"));
        status.progress = 100;

        switch (json.getString("state")) {
            case "finished":
                status.status = Status.success;
                break;
            case "failed":
            case "cancelled":
                status.status = Status.fail;
                break;
            default:
                throw new IllegalStateException("Unexpected input state: " + json.getString("state"));
        }

        ImmutableList.Builder<String> files = ImmutableList.builder();
        JSONArray jsonFiles = json.getJSONArray("output_media_files");
        for (int i = 0; i < jsonFiles.length(); i++) {
            files.add(Uri.parse(jsonFiles.getJSONObject(i).getString("url")).getPath().substring(1));
        }

        status.files = files.build();

        ImmutableList.Builder<String> screenshots = ImmutableList.builder();
        JSONArray jsonScreenshots = json.getJSONArray("thumbnails");
        for (int i = 0; i < jsonScreenshots.length(); i++) {
            screenshots.add(Uri.parse(jsonScreenshots.getJSONObject(i).getString("url")).getPath().substring(1));
        }

        status.screenshots = screenshots.build();

        return status;
    }

    public String getId() {
        return Integer.toString(id);
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

    public Integer getProgress() {
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ZencoderConversionStatus that = (ZencoderConversionStatus) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(status, that.status)
            .append(files, that.files)
            .append(screenshots, that.screenshots)
            .append(created, that.created)
            .append(updated, that.updated)
            .append(progress, that.progress)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(status)
            .append(files)
            .append(screenshots)
            .append(created)
            .append(updated)
            .append(progress)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("id", id)
            .append("status", status)
            .append("files", files)
            .append("screenshots", screenshots)
            .append("created", created)
            .append("updated", updated)
            .append("progress", progress)
            .toString();
    }
}
