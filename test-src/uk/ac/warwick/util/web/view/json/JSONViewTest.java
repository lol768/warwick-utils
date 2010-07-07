package uk.ac.warwick.util.web.view.json;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class JSONViewTest {
    
    @Test
    public void contentType() throws Exception {
        JSONView view = new JSONView() {
            @Override
            public JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
                return new JSONObject();
            }
        };
        view.setJsonpRequestValidator(JSONPRequestValidator.ALLOW_ALL);
        
        assertEquals("application/json", view.getContentType());
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> model = Maps.newHashMap();
        
        view.render(model, request, response);
        
        assertEquals("application/json", response.getContentType());
        
        //JSONP should have a different content type
        response = new MockHttpServletResponse();
        request.setParameter("jsonp", "callback");
        
        view.render(model, request, response);
        
        assertEquals("application/javascript", response.getContentType());
    }
    
    @Test
    public void render() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final Map<String, Object> model = Maps.newHashMap();
        model.put("a", "b");
     
        JSONView view = new JSONView() {
            @Override
            public JSONObject render(Map<String, Object> innerModel, List<String> errors) throws Exception {
                assertTrue(errors.isEmpty());
                assertEquals(model, innerModel);
                
                errors.add("error");
                
                return new JSONObject() {{
                   put("a", "b"); 
                }};
            }
        };
        
        view.render(model, request, response);
        
        assertEquals("{\"errors\":[\"error\"],\"data\":{\"a\":\"b\"}}", response.getContentAsString());
    }
    
    @Test
    public void combine() throws Exception {
        JSONView view1 = new JSONView() {
            @Override
            public JSONObject render(Map<String, Object> innerModel, List<String> errors) throws Exception {
                errors.add("error1");
                
                return new JSONObject() {{
                   put("a", 1);
                   put("b", true);
                   put("c", JSONObject.NULL);
                }};
            }
        };
        
        JSONView view2 = new JSONView() {
            @Override
            public JSONObject render(Map<String, Object> innerModel, List<String> errors) throws Exception {
                errors.add("error2");
                errors.add("error3");
                
                return new JSONObject() {{
                   put("c", new JSONArray() {{ put("a"); put("b"); put("c"); }});
                   put("d", "yes");
                   put("E", 0.1d);
                }};
            }
        };
        
        JSONView combined = JSONView.combine(view1, view2);
        
        List<String> errors = Lists.newArrayList();
        JSONObject object = combined.render(null, errors);
        
        assertEquals(Lists.newArrayList("error1", "error2", "error3"), errors);
        assertEquals(new JSONObject() {{
            put("a", 1);
            put("b", true);
            put("c", new JSONArray() {{ put("a"); put("b"); put("c"); }});
            put("d", "yes");
            put("E", 0.1d);
        }}.toString(), object.toString());
    }

}
