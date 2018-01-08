package uk.ac.warwick.util.mywarwick.model.response;

public class Warning extends Error {

    public Warning() {
        super();
    }

    public Warning(String id, String message) {
        this.id = id;
        this.message = message;
    }
}
