package uk.ac.warwick.util.mywarwick.model;

import javax.naming.ConfigurationException;
import java.util.Set;

public interface Configuration {
    Set<Instance> getInstances();

    void setInstances(Set<Instance> instances);

    default void validate() throws ConfigurationException {
        if (getInstances() == null)
            throw new ConfigurationException("mywarwick Configuration getInstances() returns null");
        if (getInstances().size() == 0)
            throw new ConfigurationException("mywarwick Configuration getInstances() contains 0 instances, please check your configuration(Spring property file, or Play config file)");
    }
}
