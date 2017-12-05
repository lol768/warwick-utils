package uk.ac.warwick.util.mywarwick.model;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationTest {

    ConfigurationWithEmptyInstances configurationWithEmptyInstances = new ConfigurationWithEmptyInstances();
    ConfigurationWithNullInstances configurationWithNullInstances = new ConfigurationWithNullInstances();

    @Test(expected = IllegalArgumentException.class)
    public void expectExceptionThrownWithEmptyInstances()  {
        configurationWithEmptyInstances.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void expectExceptionThrownWithNullInstances()  {
        configurationWithNullInstances.validate();
    }

}

abstract class TestConfiguration implements Configuration{
    @Override
    public int getHttpMaxConn() {
        return 0;
    }

    @Override
    public int getHttpMaxConnPerRoute() {
        return 0;
    }
}

class ConfigurationWithEmptyInstances extends TestConfiguration {

    public Set<Instance> getInstances() {
        return new HashSet<>();
    }

}

class ConfigurationWithNullInstances extends TestConfiguration {

    public Set<Instance> getInstances() {
        return null;
    }

}