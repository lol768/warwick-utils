package uk.ac.warwick.util.convert;

import com.google.common.io.ByteSource;

import java.io.IOException;

public interface DocumentConversionService {

    DocumentConversionResult convert(ByteSource in, String filename, String inputFormat, String outputFormat) throws IOException;

    ByteSource getConvertedFile(DocumentConversionResult result, String id);

}
