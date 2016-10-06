package uk.ac.warwick.util.convert;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.web.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

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

    ConversionMedia upload(ByteSource source) throws IOException;

    ConversionMedia getMediaById(String id) throws IOException;

    ConversionStatus convert(ConversionMedia media, Format format) throws IOException;

    ConversionStatus getStatus(String id) throws IOException;

    void delete(ConversionMedia media) throws IOException;

    Uri getEncodedFileUrl(ConversionStatus status) throws IOException;

    Uri getScreenshotUrl(ConversionStatus status) throws IOException;

    void getEncodedFile(ConversionStatus status, Consumer<InputStream> consumer) throws IOException;

    void getScreenshot(ConversionStatus status, Consumer<InputStream> consumer) throws IOException;

}
