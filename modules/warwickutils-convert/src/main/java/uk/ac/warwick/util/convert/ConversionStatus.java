package uk.ac.warwick.util.convert;

import java.time.ZonedDateTime;
import java.util.List;

public interface ConversionStatus {

    enum Status {
        success, fail, processing
    }

    String getId();

    Status getStatus();

    List<String> getFiles();

    List<String> getScreenshots();

    ZonedDateTime getCreated();

    ZonedDateTime getUpdated();

    Integer getProgress();

}
