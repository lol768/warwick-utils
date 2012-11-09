package uk.ac.warwick.util.content.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.google.common.collect.Maps;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public final class FreeMarkerRenderingUtils {
	
    private FreeMarkerRenderingUtils() {}

    
    public static StringWriter processTemplate(final Configuration configuration, final String templateName) {
        return processTemplate(configuration, templateName, Maps.<String, Object>newHashMap());
    }
    
    /**
     * Use a default freemarker configuration
     */
    public static StringWriter processTemplate(final String templateName, final Map<String, Object> model){
        Configuration configuration = generateConfig();
        return processTemplate(configuration, templateName, model);
    }

    public static StringWriter processTemplate(final Configuration configuration, final String templateName, final Map<String, Object> model) {

        if (configuration == null) {
            throw new IllegalStateException("Configuration cannot be null");
        }

        Template template = null;
        try {
            template = configuration.getTemplate(templateName);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot locate template " + templateName, e);
        }

        final StringWriter sw = processTemplate(template, model);

        return sw;
    }

    public static StringWriter processTemplate(final Template template, final Map<String, Object> model) {

        final StringWriter sw = new StringWriter();

        try {
            template.process(model, sw);
        } catch (final TemplateException e) {
            throw new IllegalStateException("Cannot process " + template.getName(), e);
        } catch (final IOException e) {
            throw new IllegalStateException("Unknown IOException with " + template.getName(), e);
        }

        return sw;
    }
    
    private static Configuration generateConfig(){
        Configuration configuration = new Configuration();
        configuration.setObjectWrapper(new DateTimeFreemarkerObjectWrapper());

        configuration.setDefaultEncoding("ISO-8859-1");
        configuration.setOutputEncoding("ISO-8859-1");

        // Register default template loaders.
        TemplateLoader templateLoader = new ClassTemplateLoader(FreeMarkerRenderingUtils.class, "/freemarker");
        configuration.setTemplateLoader(templateLoader);
        
        return configuration;
    }

}
