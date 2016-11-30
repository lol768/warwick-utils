package uk.ac.warwick.util.mywarwick;

import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.PropertiesConfiguration;

import java.util.*;

import static org.junit.Assert.*;

public class PropertiesConfigurationTest {

    PropertiesConfiguration propertiesConfiguration;

    @Before
    public void setup() {
        Properties properties = new Properties();
        properties.setProperty("mywarwick.instances.0.baseUrl", "1");
        properties.setProperty("mywarwick.instances.0.providerId", "2");
        properties.setProperty("mywarwick.instances.0.userName", "3");
        properties.setProperty("mywarwick.instances.0.password", "4");
        properties.setProperty("mywarwick.instances.1.baseUrl", "5");
        properties.setProperty("mywarwick.instances.1.providerId", "6");
        properties.setProperty("mywarwick.instances.1.userName", "7");
        properties.setProperty("mywarwick.instances.1.password", "8");
        propertiesConfiguration = new PropertiesConfiguration(properties);
    }

    @Test
    public void shouldReadPropertiesFileAndFormatToConfiguration() {
        Set<Instance> expected = new HashSet<>();
        expected.add(new Instance("1", "2", "3", "4"));
        expected.add(new Instance("5", "6", "7", "8"));
        assertEquals(expected, propertiesConfiguration.getInstances());
    }

}