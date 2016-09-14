package uk.ac.warwick.util.convert;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

public interface ConversionStatus {

    enum Status {
        success, fail, processing
    }

    String getId();

    String getVideoId();

    Status getStatus();

    List<String> getFiles();

    List<String> getScreenshots();

    DateTime getCreated();

    DateTime getUpdated();

    DateTime getStartedEncoding();

    Duration getEncodingTime();

    Integer getProgress();

    Integer getWidth();

    Integer getHeight();

    Duration getDuration();
}
