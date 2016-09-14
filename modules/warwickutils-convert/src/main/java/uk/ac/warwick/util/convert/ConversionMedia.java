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

public interface ConversionMedia {

    enum Status {
        success, fail, processing
    }

    String getId();

    String getOriginalFilename();

    DateTime getCreated();

    DateTime getUpdated();

    Status getStatus();

    Integer getWidth();

    Integer getHeight();

    Duration getDuration();
}
