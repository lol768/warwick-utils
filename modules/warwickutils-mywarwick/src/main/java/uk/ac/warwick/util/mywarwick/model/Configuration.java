package uk.ac.warwick.util.mywarwick.model;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public interface Configuration {
    Set<Instance> getInstances();

    int getHttpMaxConn();
    int getHttpMaxConnPerRoute();

    default void validate() throws IllegalArgumentException {
        if (getInstances() == null)
            throw new IllegalArgumentException("mywarwick Configuration getInstances() returns null");
        if (getInstances().size() == 0)
            throw new IllegalArgumentException("mywarwick Configuration getInstances() contains 0 instances, please check your configuration(Spring property file, or Play config file)");
    }

    static Properties loadDefaults() {
        Properties properties = new Properties();
        try {
            properties.load(PropertiesConfiguration.class.getResourceAsStream("mywarwick-defaults.properties"));
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("defaults.properties missing");
        }
    }
}
