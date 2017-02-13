package uk.ac.warwick.util.convert;

import org.joda.time.DateTime;

import java.util.List;

public interface ConversionStatus {

    enum Status {
        success, fail, processing
    }

    String getId();

    Status getStatus();

    List<String> getFiles();

    List<String> getScreenshots();

    DateTime getCreated();

    DateTime getUpdated();

    Integer getProgress();

}
