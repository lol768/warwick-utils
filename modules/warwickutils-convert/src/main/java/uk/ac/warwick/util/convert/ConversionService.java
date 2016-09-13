package uk.ac.warwick.util.convert;

import uk.ac.warwick.util.web.Uri;

import java.io.File;
import java.io.IOException;

public interface ConversionService {

    enum Format {
        h264("h264-constrain"),
        webm("webm-constrain"),
        jpg("thumbnail-constrain"),
        mp3("mp3");

        private final String profileName;

        Format(String profileName) {
            this.profileName = profileName;
        }

        public String getProfileName() {
            return profileName;
        }
    }

    ConversionMedia upload(File file) throws IOException;

    ConversionMedia getMediaById(String id) throws IOException;

    ConversionStatus convert(ConversionMedia media, Format format) throws IOException;

    ConversionStatus getStatus(String id) throws IOException;

    void delete(ConversionMedia media) throws IOException;

    Uri getEncodedFileUrl(ConversionStatus status) throws IOException;

    Uri getScreenshotUrl(ConversionStatus status) throws IOException;

}
