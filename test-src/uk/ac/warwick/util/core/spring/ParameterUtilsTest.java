package uk.ac.warwick.util.core.spring;

import java.util.Map;

import org.junit.Test;
import org.springframework.web.bind.ServletRequestBindingException;

import com.google.common.collect.Maps;


public class ParameterUtilsTest {
    @Test(timeout=1000, expected=ServletRequestBindingException.class) 
    public void doubleParsing() throws Exception {
        Map<String, String[]> request = Maps.newHashMap();
        request.put("quantity", new String[]{"2.2250738585072012e-308"});
        ParameterUtils.getDoubleParameter(request, "quantity");
    }
}
