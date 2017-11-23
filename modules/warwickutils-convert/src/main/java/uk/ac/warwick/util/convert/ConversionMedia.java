package uk.ac.warwick.util.convert;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface ConversionMedia {

    enum Status {
        success, fail, processing
    }

    String getId();

    String getOriginalFilename();

    ZonedDateTime getCreated();

    ZonedDateTime getUpdated();

    Status getStatus();

    Integer getWidth();

    Integer getHeight();

    Duration getDuration();
}
