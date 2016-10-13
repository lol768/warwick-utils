package uk.ac.warwick.util.convert.cloudconvert;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import uk.ac.warwick.util.convert.DocumentConversionResult;

import java.util.Collections;
import java.util.List;

public class CloudConvertDocumentConversionResult implements DocumentConversionResult {

    private final String conversionId;

    private final boolean successful;

    private final String errorMessage;

    private final List<String> s3Keys;

    private CloudConvertDocumentConversionResult(String conversionId, boolean successful, List<String> s3Keys, String errorMessage) {
        this.conversionId = conversionId;
        this.successful = successful;
        this.s3Keys = s3Keys;
        this.errorMessage = errorMessage;
    }

    public static CloudConvertDocumentConversionResult success(String conversionId, List<String> s3Keys) {
        return new CloudConvertDocumentConversionResult(conversionId, true, s3Keys, null);
    }

    public static CloudConvertDocumentConversionResult error(String conversionId, String message) {
        return new CloudConvertDocumentConversionResult(conversionId, false, Collections.emptyList(), message);
    }

    @Override
    public String getConversionId() {
        return conversionId;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public List<String> getConvertedFileIds() {
        return s3Keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CloudConvertDocumentConversionResult that = (CloudConvertDocumentConversionResult) o;

        return new EqualsBuilder()
            .append(successful, that.successful)
            .append(conversionId, that.conversionId)
            .append(errorMessage, that.errorMessage)
            .append(s3Keys, that.s3Keys)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(conversionId)
            .append(successful)
            .append(errorMessage)
            .append(s3Keys)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("conversionId", conversionId)
            .append("successful", successful)
            .append("errorMessage", errorMessage)
            .append("s3Keys", s3Keys)
            .toString();
    }

}
