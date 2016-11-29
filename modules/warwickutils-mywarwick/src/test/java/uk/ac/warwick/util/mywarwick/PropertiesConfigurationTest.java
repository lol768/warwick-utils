package uk.ac.warwick.util.mywarwick;

import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.model.Instance;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.junit.Assert.*;

public class PropertiesConfigurationTest {

    PropertiesConfiguration propertiesConfiguration;

    @Before
    public void setup() {
        propertiesConfiguration = new PropertiesConfiguration();
        Properties properties = new Properties();
        properties.setProperty("mywarwick.services.0.baseUrl", "1");
        properties.setProperty("mywarwick.services.0.providerId", "2");
        properties.setProperty("mywarwick.services.0.userName", "3");
        properties.setProperty("mywarwick.services.0.password", "4");
        properties.setProperty("mywarwick.services.1.baseUrl", "5");
        properties.setProperty("mywarwick.services.1.providerId", "6");
        properties.setProperty("mywarwick.services.1.userName", "7");
        properties.setProperty("mywarwick.services.1.password", "8");
        propertiesConfiguration.setApplicationProperties(properties);
    }

    @Test
    public void shouldReadPropertiesFileAndFormatToConfiguration() {
        List<Instance> expected = new ArrayList<>();
        expected.add(new Instance("1", "2", "3", "4"));
        expected.add(new Instance("5", "6", "7", "8"));

        assertEquals(expected, propertiesConfiguration.getInstances());
    }

}