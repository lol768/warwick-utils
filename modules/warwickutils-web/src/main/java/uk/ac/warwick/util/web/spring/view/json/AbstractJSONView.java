package uk.ac.warwick.util.web.spring.view.json;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.View;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

@Configurable
abstract class AbstractJSONView<T> implements View {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJSONView.class);
    
    private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;
    
    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
    
    @Autowired(required = true)
    private transient JSONPRequestValidator jsonpRequestValidator = JSONPRequestValidator.REJECT_ALL;
    
    private boolean wrapErrors = true;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSON<?> results = getValue(model, request);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
        OutputStreamWriter writer = new OutputStreamWriter(baos, getCharacterEncoding());
        try {
            String contentType = getContentType();
            
            // jsonp - referer check is mandatory because otherwise any website would be able to get our information
            String callback = getParam(request, "jsonp", "callback");
            if (StringUtils.hasText(callback) && jsonpRequestValidator.isAllow(request)) {
                writer.write(callback + "(");
                results.write(writer);
                writer.write(");");
                
                // For JSONP requests we should serve application/javascript as the content type
                contentType = "application/javascript";
            } else {
                String assignment = getParam(request, "assign");
                if (StringUtils.hasText(assignment) && jsonpRequestValidator.isAllow(request)) {
                    writer.write("var " + assignment + " = ");
                    results.write(writer);
                    writer.write(";");
                    
                    // For JSONP requests we should serve application/javascript as the content type
                    contentType = "application/javascript";                    
                } else {
                    results.write(writer);
                }
            }
            
            writer.flush();
            
            writeToResponse(response, baos, contentType);
        } finally {
            writer.close();
        }
    }

    private JSON<?> getValue(Map<String, Object> model, HttpServletRequest request) throws JSONException, ServletException {
        if (wrapErrors) {
            JSONObject results = new JSONObject();
            JSONArray errors = new JSONArray();
            JSON<T> data = null;
            
            List<String> errorsList = Lists.newArrayList();
            
            try {
                data = renderToJSON(model, request, errorsList);
                
                for (String error : errorsList) {
                    errors.put(error);
                }
            } catch (Exception e) {
                errors.put("Uncaught exception: " + e.getMessage());
                LOGGER.error("Error processing JSON view", e);
            }
            
            results.put("data", data == null ? JSONObject.NULL : data.unwrap());
            results.put("errors", errors);
            return JSON.wrap(results);
        } else {
            try {
                return renderToJSON(model, request, Lists.<String>newArrayList());
            } catch (Exception e) {
                LOGGER.error("Error processing JSON view", e);
                throw new ServletException(e);
            }
        }
    }

    /**
     * @return a {@link JSON<T>} of the results
     */
    public abstract JSON<T> renderToJSON(Map<String, Object> model, HttpServletRequest request, List<String> errors) throws Exception;
    
    private void writeToResponse(HttpServletResponse response, ByteArrayOutputStream baos, String contentType) throws IOException {
        // Write content type and also length (determined via byte array).
        response.setContentType(contentType);
        response.setContentLength(baos.size());

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        baos.writeTo(out);
        out.flush();
    }
    
    private String getParam(HttpServletRequest request, String... params) throws ServletRequestBindingException {
        for (String param : params) {
            String result = ServletRequestUtils.getStringParameter(request, param);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    public String getCharacterEncoding() {
        return DEFAULT_CHARACTER_ENCODING;
    }

    public final String getContentType() {
        // Needs to be application/json in order to use proper json parsing in prototype
        return "application/json";
    }

    public final void setJsonpRequestValidator(JSONPRequestValidator jsonpRequestValidator) {
        this.jsonpRequestValidator = jsonpRequestValidator;
    }
    
    public final JSONPRequestValidator getJsonpRequestValidator() {
        return jsonpRequestValidator;
    }

    public final void setWrapErrors(boolean wrapErrors) {
        this.wrapErrors = wrapErrors;
    }
    
    public final void setJsonpRequestValidatorAsString(String validator) {
    	if ("ALLOW_ALL".equals(validator)) {
    		this.jsonpRequestValidator = JSONPRequestValidator.ALLOW_ALL;
    	} else if ("REJECT_ALL".equals(validator)) {
    		this.jsonpRequestValidator = JSONPRequestValidator.REJECT_ALL;
    	} else {
    		throw new IllegalArgumentException();
    	}
    }

}
