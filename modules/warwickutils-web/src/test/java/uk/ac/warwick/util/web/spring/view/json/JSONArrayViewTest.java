package uk.ac.warwick.util.web.spring.view.json;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class JSONArrayViewTest {
    
    @Test
    public void contentType() throws Exception {
        JSONArrayView view = new JSONArrayView() {
            @Override
            public JSONArray render(Map<String, Object> model, HttpServletRequest request, List<String> errors) throws Exception {
                return new JSONArray();
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
     
        JSONArrayView view = new JSONArrayView() {
            @Override
            public JSONArray render(Map<String, Object> innerModel, HttpServletRequest request, List<String> errors) throws Exception {
                assertTrue(errors.isEmpty());
                assertEquals(model, innerModel);
                
                errors.add("error");
                
                return new JSONArray() {{
                   put("a");
                   put("b"); 
                }};
            }
        };
        
        view.render(model, request, response);

        assertEquals(
            new JSONObject("{\"data\":[\"a\",\"b\"],\"errors\":[\"error\"]}").toString(),
            new JSONObject(response.getContentAsString()).toString()
        );
    }
    
    @Test
    public void combine() throws Exception {
        JSONArrayView view1 = new JSONArrayView() {
            @Override
            public JSONArray render(Map<String, Object> innerModel, HttpServletRequest request, List<String> errors) throws Exception {
                errors.add("error1");
                
                return new JSONArray() {{
                   put(1);
                   put(true);
                   put(JSONObject.NULL);
                }};
            }
        };
        
        JSONArrayView view2 = new JSONArrayView() {
            @Override
            public JSONArray render(Map<String, Object> innerModel, HttpServletRequest request, List<String> errors) throws Exception {
                errors.add("error2");
                errors.add("error3");
                
                return new JSONArray() {{
                   put(new JSONArray() {{ put("a"); put("b"); put("c"); }});
                   put("yes");
                   put(0.1d);
                }};
            }
        };
        
        JSONArrayView combined = JSONArrayView.combine(view1, view2);
        
        List<String> errors = Lists.newArrayList();
        JSONArray array = combined.render(null, new MockHttpServletRequest(), errors);
        
        assertEquals(Lists.newArrayList("error1", "error2", "error3"), errors);
        assertEquals(new JSONArray() {{
            put(1);
            put(true);
            put(JSONObject.NULL);
            put(new JSONArray() {{ put("a"); put("b"); put("c"); }});
            put("yes");
            put(0.1d);
        }}.toString(), array.toString());
    }

}
