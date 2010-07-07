package uk.ac.warwick.util.web.view.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.View;

import com.google.common.collect.Lists;

@Configurable
public abstract class JSONView implements View {
    
    private static final Logger LOGGER = Logger.getLogger(JSONView.class);
    
    private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;
    
    @Autowired(required = true)
    private transient JSONPRequestValidator jsonpRequestValidator = JSONPRequestValidator.REJECT_ALL;

    @SuppressWarnings("unchecked")
    public final void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject results = new JSONObject();
        JSONArray errors = new JSONArray();
        JSONObject data = new JSONObject();
        
        List<String> errorsList = Lists.newArrayList();
        
        try {
            data = render(model, errorsList);
            
            for (String error : errorsList) {
                errors.put(error);
            }
        } catch (Exception e) {
            errors.put("Uncaught exception: " + e.getMessage());
            LOGGER.error("Error processing JSON view", e);
        }
        
        results.put("data", data == null ? JSONObject.NULL : data);
        results.put("errors", errors);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
        OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
        try {
            String contentType = getContentType();
            
            // jsonp - referer check is mandatory because otherwise any website would be able to get our information
            String callback = ServletRequestUtils.getStringParameter(request, "jsonp");
            if (StringUtils.hasText(callback) && jsonpRequestValidator.isAllow(request)) {
                writer.write(callback + "(");
                results.write(writer);
                writer.write(");");
                
                // For JSONP requests we should serve application/javascript as the content type
                contentType = "application/javascript";
            } else {
                results.write(writer);
            }
            
            writer.flush();
            
            writeToResponse(response, baos, contentType);
        } finally {
            writer.close();
        }
    }
    
    private void writeToResponse(HttpServletResponse response, ByteArrayOutputStream baos, String contentType) throws IOException {
        // Write content type and also length (determined via byte array).
        response.setContentType(contentType);
        response.setContentLength(baos.size());

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        baos.writeTo(out);
        out.flush();
    }

    public final void setJsonpRequestValidator(JSONPRequestValidator jsonpRequestValidator) {
        this.jsonpRequestValidator = jsonpRequestValidator;
    }

    public final String getContentType() {
        // Needs to be application/json in order to use proper json parsing in prototype
        return "application/json";
    }

    protected static long toJSON(DateTime dt) {
        return dt == null ? 0 : dt.getMillis();
    }

    /**
     * @return a {@link JSONObject} of the results
     */
    public abstract JSONObject render(Map<String, Object> model, List<String> errors) throws Exception;
    
    public static JSONView combine(final JSONView... views) {
        return new JSONView() {
            @SuppressWarnings("unchecked")
            @Override
            public JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
                JSONObject combined = new JSONObject();
                for (JSONView view : views) {
                    JSONObject object = view.render(model, errors);
                    for (Iterator<String> itr = object.keys(); itr.hasNext();) {
                        String key = itr.next();
                        combined.put(key, object.get(key));
                    }
                }
                
                return combined;
            }            
        };
    }
    
    public static JSONView of(final JSONObject object) {
        return new JSONView() {
            @Override
            public JSONObject render(Map<String, Object> model, List<String> errors) throws Exception {
                return object;
            }            
        };
    }

}
