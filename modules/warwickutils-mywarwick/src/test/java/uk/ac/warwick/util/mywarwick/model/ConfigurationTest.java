package uk.ac.warwick.util.mywarwick.model;
import org.junit.Test;
import javax.naming.ConfigurationException;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationTest {

    ConfigurationWithEmptyInstances configurationWithEmptyInstances = new ConfigurationWithEmptyInstances();
    ConfigurationWithNullInstances configurationWithNullInstances = new ConfigurationWithNullInstances();

    @Test(expected = ConfigurationException.class)
    public void expectExceptionThrownWithEmptyInstances() throws ConfigurationException {
        configurationWithEmptyInstances.validate();
    }

    @Test(expected = ConfigurationException.class)
    public void expectExceptionThrownWithNullInstances() throws ConfigurationException {
        configurationWithNullInstances.validate();
    }

}

class ConfigurationWithEmptyInstances implements Configuration{

    @Override
    public Set<Instance> getInstances() {
        return new HashSet<>();
    }

    @Override
    public void setInstances(Set<Instance> instances) {

    }
}


class ConfigurationWithNullInstances implements Configuration{

    @Override
    public Set<Instance> getInstances() {
        return null;
    }

    @Override
    public void setInstances(Set<Instance> instances) {

    }
}