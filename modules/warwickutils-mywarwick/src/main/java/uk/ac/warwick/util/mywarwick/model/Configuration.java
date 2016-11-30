package uk.ac.warwick.util.mywarwick.model;
import java.util.Set;

public interface Configuration {
    Set<Instance> getInstances();

    void setInstances(Set<Instance> instances);

    default void validate() throws IllegalArgumentException {
        if (getInstances() == null)
            throw new IllegalArgumentException("mywarwick Configuration getInstances() returns null");
        if (getInstances().size() == 0)
            throw new IllegalArgumentException("mywarwick Configuration getInstances() contains 0 instances, please check your configuration(Spring property file, or Play config file)");
    }
}
