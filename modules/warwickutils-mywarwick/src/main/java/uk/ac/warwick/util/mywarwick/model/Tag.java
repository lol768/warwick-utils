package uk.ac.warwick.util.mywarwick.model;

public class Tag {
    private String name;
    private String value;
    private String display_value;

    public Tag() {
        name = "";
        value = "";
        display_value = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplay_value() {
        return display_value;
    }

    public void setDisplay_value(String display_value) {
        this.display_value = display_value;
    }
}
