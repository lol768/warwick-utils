package uk.ac.warwick.util.web.tags;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public final class EnumValuesTagTest {
    
    public enum TestEnum {
        one, two, three
    }

    @Test
    public void getValues() throws Exception {
        String className = TestEnum.class.getName();

        assertEquals(Arrays.asList(TestEnum.values()), EnumValuesTag.getValues(className));
    }

    @Test(expected = IllegalStateException.class)
    public void getValuesNotEnum() throws Exception {
        String className = EnumValuesTag.class.getName();
        EnumValuesTag.getValues(className);
        
        fail("Should have exceptioned");
    }

}
