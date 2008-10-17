package uk.ac.warwick.util.content.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public final class FreeMarkerRenderingUtils {
	private static final Logger LOGGER = Logger.getLogger(FreeMarkerRenderingUtils.class);
	
    private FreeMarkerRenderingUtils() {
    }

    public static StringWriter processTemplate(final String templateName, final Map<String, Object> model) {
    	Configuration configuration = ConfigurationFactory.getConfiguration();
    	
        if (configuration == null) {
        	throw new IllegalStateException("Configuration cannot be null");
        }
        
        Template template = null;
        try {
            template = configuration.getTemplate(templateName);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot locate template " + templateName, e);
        }
        StringWriter sw = new StringWriter();

        try {            
            template.process(model, sw);
        } catch (final TemplateException e) {
        	LOGGER.error("Could not process template " + templateName, e);
            throw new IllegalStateException("Cannot process " + templateName, e);
        } catch (final IOException e) {
        	LOGGER.error("Could not process template " + templateName, e);
            throw new IllegalStateException("Unknown IOException with " + templateName, e);
        }
        
        return sw;
    }

}
