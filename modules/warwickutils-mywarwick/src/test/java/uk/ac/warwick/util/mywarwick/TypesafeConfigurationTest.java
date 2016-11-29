package uk.ac.warwick.util.mywarwick;

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import uk.ac.warwick.util.mywarwick.model.Instance;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TypesafeConfigurationTest {

    com.typesafe.config.Config typeSafeConfigProperties;

    private TypesafeConfiguration typesafeConfigs = new TypesafeConfiguration();

    @Before
    public void setup() {
        typeSafeConfigProperties = ConfigFactory.parseString("mywarwick.services = [\n" +
                "   {\n" +
                "    baseUrl=\"1\"\n" +
                "    providerId=\"2\"\n" +
                "    userName=\"3\"\n" +
                "    password=\"4\"\n" +
                "  }\n" +
                "  {\n" +
                "    baseUrl=\"5\"\n" +
                "    providerId=\"6\"\n" +
                "    userName=\"7\"\n" +
                "    password=\"8\"\n" +
                "  }\n" +
                "]");
        typesafeConfigs.setTypeSafeConfigProperties(typeSafeConfigProperties);
    }


    @Test
    public void shouldLoadAndFormatTypeSafeConfigsIntoConfigs() {
        List<Instance> expected = new ArrayList<>();
        expected.add(new Instance("1", "2", "3", "4"));
        expected.add(new Instance("5", "6", "7", "8"));
        assertEquals(expected, typesafeConfigs.getInstances());
    }
}