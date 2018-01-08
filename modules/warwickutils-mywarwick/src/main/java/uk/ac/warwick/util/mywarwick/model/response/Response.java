package uk.ac.warwick.util.mywarwick.model.response;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class Response {

    Boolean success;
    String status;
    Data data;
    List<Error> errors;
    List<Warning> Warnings;

    public Response() {
        this.errors = new ArrayList<>();
    }

    public Response(Boolean success, String status, Data data, List<Error> errors) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.errors = errors;
    }

    public Response(Boolean success, String status, Data data, Error error) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.errors = new ArrayList<>();
        this.errors.add(error);
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

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public void setError(Error error) {
        if (this.errors == null) this.errors = new ArrayList<>();
        this.errors.add(error);
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
                .append(getErrors(), response.getErrors())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getSuccess())
                .append(getStatus())
                .append(getData())
                .append(getErrors())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", status='" + status + '\'' +
                ", data=" + data +
                ", errors=" + errors +
                '}';
    }
}
