package uk.ac.warwick.util.core.spring;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.web.bind.ServletRequestBindingException;

import uk.ac.warwick.util.core.StopWatch;

import com.google.common.collect.Maps;


public class ParameterUtilsTest {
    
    final Pattern p = Pattern.compile("[0.]*2\\.?2\\.?2\\.?5\\.?0\\.?7\\.?3\\.?8\\.?5\\.?8\\.?5\\.?0\\.?7\\.?2\\.?0\\.?1\\.?2\\.(0\\.?)*?e-\\d+");
    final String prefix = "22250738";
    final String suffix = "8585072012";
    
    final String in = "2.2250738585072012e-308";
    
    @Test(timeout=1000) 
    public void doubleParsing() throws Exception {
        for (String s : new String[]{
            "2.2250738585072012e-308",
            "0.00022250738585072012e-304",
            "22.250738585072012e-309"
            }) {        
            try {
                System.out.println(s);
                Map<String, String[]> request = Maps.newHashMap();
                request.put("quantity", new String[]{s});
                ParameterUtils.getRequiredDoubleParameter(request, "quantity");
                fail("Shoulda thrown except");
            } catch (ServletRequestBindingException e) {
                assertEquals(NumberFormatException.class, e.getCause().getClass());
            }
        }
    }

}
