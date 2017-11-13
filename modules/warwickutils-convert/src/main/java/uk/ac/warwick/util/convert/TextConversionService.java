package uk.ac.warwick.util.convert;

import com.google.common.io.ByteSource;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public interface TextConversionService {

    boolean canConvert(ContentType from, ContentType to);

    /**
     * @throws IOException if there was a problem opening the input
     * @throws ConversionException if there was a problem converting
     */
    ByteSource convert(ByteSource in, ContentType from, ContentType to) throws IOException;

}
