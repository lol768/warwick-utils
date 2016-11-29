package uk.ac.warwick.util.mywarwick;

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TypeSafeConfigsTest {

    com.typesafe.config.Config typeSafeConfigProperties = ConfigFactory.load("typesafeconfig.conf");

    private TypeSafeConfigs typeSafeConfigs = new TypeSafeConfigs();

    @Before
    public void setup() {
        typeSafeConfigs.setTypeSafeConfigProperties(typeSafeConfigProperties);
    }


    @Test @Ignore
    public void shouldLoadandFormatTypeSafeConfigsIntoConfigs() {
        List<uk.ac.warwick.util.mywarwick.model.Config> expected = new ArrayList<>();
        expected.add(new uk.ac.warwick.util.mywarwick.model.Config("1", "2", "3", "4"));
        expected.add(new uk.ac.warwick.util.mywarwick.model.Config("5", "6", "7", "8"));
        assertEquals(expected, typeSafeConfigs.getConfigs());
    }
}