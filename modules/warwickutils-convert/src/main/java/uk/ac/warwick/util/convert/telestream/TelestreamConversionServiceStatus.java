package uk.ac.warwick.util.convert.telestream;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class TelestreamConversionServiceStatus {

    private ImmutableList<TelestreamConversionStatus> encodings;

    public static TelestreamConversionServiceStatus fromJSON(JSONArray json) throws JSONException {
        TelestreamConversionServiceStatus status = new TelestreamConversionServiceStatus();

        ImmutableList.Builder<TelestreamConversionStatus> encodings = ImmutableList.builder();
        for (int i = 0; i < json.length(); i++) {
            encodings.add(TelestreamConversionStatus.fromJSON(json.getJSONObject(i)));
        }
        status.encodings = encodings.build();

        return status;
    }

    public List<TelestreamConversionStatus> getEncodings() {
        return encodings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TelestreamConversionServiceStatus that = (TelestreamConversionServiceStatus) o;

        return new EqualsBuilder()
            .append(encodings, that.encodings)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(encodings)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("encodings", encodings)
            .toString();
    }
}
