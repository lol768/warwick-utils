package uk.ac.warwick.util.mywarwick.model.response;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Response {
    Boolean success;
    String status;
    Data data;
    Error error;

    public Response() {
        super();
    }

    public Response(Boolean success, String status, Data data, Error error) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Response response = (Response) o;

        return new EqualsBuilder()
                .append(getSuccess(), response.getSuccess())
                .append(getStatus(), response.getStatus())
                .append(getData(), response.getData())
                .append(getError(), response.getError())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getSuccess())
                .append(getStatus())
                .append(getData())
                .append(getError())
                .toHashCode();
    }
}
