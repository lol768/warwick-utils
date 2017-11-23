package uk.ac.warwick.util.content.freemarker;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * Custom object wrapper for Freemarker, allowing us to change the behaviour of
 * built-ins.
 * <p>
 * This is used to add JSR310 support to convert them automatically to
 * built-in Dates.
 * 
 * @author Mat
 */
public final class DateTimeFreemarkerObjectWrapper extends DefaultObjectWrapper {
    
    public TemplateModel wrap(Object object) throws TemplateModelException {
        if (object instanceof Instant) {
            Instant instant = (Instant)object;
            Date date = Date.from(instant);

            return super.wrap(date);
        } else if (object instanceof ChronoLocalDate) {
            ChronoLocalDate instant = (ChronoLocalDate)object;
            Date date = Date.from(instant.atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.of("Europe/London")));

            return super.wrap(date);
        } else if (object instanceof ChronoLocalDateTime<?>) {
            ChronoLocalDateTime<?> instant = (ChronoLocalDateTime<?>)object;
            Date date = Date.from(instant.toInstant(ZoneOffset.of("Europe/London")));

            return super.wrap(date);
        } else if (object instanceof ChronoZonedDateTime<?>) {
            ChronoZonedDateTime<?> instant = (ChronoZonedDateTime<?>)object;
            Date date = Date.from(instant.toInstant());

            return super.wrap(date);
        }
        
        return super.wrap(object);
    }

}
