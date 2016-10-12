package uk.ac.warwick.util.convert;

import com.google.common.io.ByteSource;

import java.io.IOException;

public interface DocumentConversionService {

    ByteSource convert(ByteSource in, String filename, String inputFormat, String outputFormat) throws IOException;

}
