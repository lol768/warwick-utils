package uk.ac.warwick.util.mywarwick.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Warning extends Error {

    public Warning() {
        super();
    }

    public Warning(String id, String message) {
        super(id, message);
    }
}
