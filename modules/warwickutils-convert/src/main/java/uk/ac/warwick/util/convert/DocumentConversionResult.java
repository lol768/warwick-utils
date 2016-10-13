package uk.ac.warwick.util.convert;

import java.util.List;

public interface DocumentConversionResult {

    String getConversionId();

    boolean isSuccessful();

    String getErrorMessage();

    List<String> getConvertedFileIds();

}
