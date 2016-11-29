package uk.ac.warwick.util.mywarwick.model;

import java.util.List;

public interface Configuration {
    List<Instance> getInstances();
    void setInstances(List<Instance> instances);
}
