package uk.ac.warwick.util.mywarwick.model;
import java.util.Set;

public interface Configuration {
    Set<Instance> getInstances();
    void setInstances(Set<Instance> instances);
}
