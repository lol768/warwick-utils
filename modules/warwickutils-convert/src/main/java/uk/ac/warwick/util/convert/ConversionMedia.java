package uk.ac.warwick.util.convert;

import org.joda.time.DateTime;
import org.joda.time.Duration;

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
