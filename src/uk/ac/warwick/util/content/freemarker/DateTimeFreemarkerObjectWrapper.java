package uk.ac.warwick.util.content.freemarker;

import java.util.Date;

import org.joda.time.ReadableInstant;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Custom object wrapper for Freemarker, allowing us to change the behaviour of
 * built-ins.
 * <p>
 * This is used to add JodaTime support to convert them automatically to
 * built-in Dates.
 * 
 * @author Mat
 */
public final class DateTimeFreemarkerObjectWrapper extends DefaultObjectWrapper {
    
    public TemplateModel wrap(Object object) throws TemplateModelException {
        if (object instanceof ReadableInstant) {
            ReadableInstant instant = (ReadableInstant)object;
            Date date = new Date(instant.getMillis());
            
            return super.wrap(date);
        }
        
        return super.wrap(object);
    }

}
