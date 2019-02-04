package uk.ac.warwick.util.content.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.*;

public final class DateTimeFreemarkerObjectWrapperTest {
    
    private TimeZone defaultTz;
    private Locale defaultLocale;
    
    @Before
    public void setUp() throws Exception {
        this.defaultTz = TimeZone.getDefault();
        this.defaultLocale = Locale.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        Locale.setDefault(Locale.UK);
    }
    
    @After
    public void tearDown() throws Exception {
        TimeZone.setDefault(defaultTz);
        Locale.setDefault(defaultLocale);
    }
    
    @Test 
    public void jsr310JavaTimeWrapping() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(getClass(), "");
        configuration.setObjectWrapper(new DateTimeFreemarkerObjectWrapper());

        Template template = configuration.getTemplate("datetime-test.ftl");
        
        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("greeting", "hello");
        
        StringWriter out = new StringWriter();
        rootMap.put("d", new Date(1234567890000L));
        template.process(rootMap, out);
        assertEquals("start;13 February 2009 23:31:30 GMT;end", out.toString());
        
        out = new StringWriter();
        rootMap.put("d", Instant.ofEpochMilli(1234567890000L));
        template.process(rootMap, out);
        assertEquals("start;13 February 2009 23:31:30 GMT;end", out.toString());
        
        out = new StringWriter();
        ZonedDateTime dateTime = LocalDateTime.of(2009, Month.JULY, 1, 1, 2, 3, 4).atZone(ZoneId.of("Europe/London"));
        assertEquals("Europe/London", dateTime.getZone().getId());
        rootMap.put("d", dateTime);
        template.process(rootMap, out);
        
        assertEquals("start;01 July 2009 01:02:03 BST;end", out.toString());
    }
    
    @Test
    public void jsr310ToDateConversion() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        
        {
            ZonedDateTime dateTime = LocalDateTime.of(2009, Month.FEBRUARY, 13, 23, 31, 30, 0).atZone(ZoneId.of("Europe/London"));
            assertEquals("Europe/London", dateTime.getZone().getId());
            assertEquals("Friday, 13 February 2009 23:31:30 o'clock GMT", formatter.format(dateTime));
            assertEquals(1234567890000L, dateTime.toInstant().toEpochMilli());
            
            Date date = Date.from(dateTime.toInstant());
            assertEquals(1234567890000L, date.getTime());
        }
        
        // now let's try a BST one
        {
            ZonedDateTime dateTime = LocalDateTime.of(2009, Month.JULY, 13, 23, 31, 30, 0).atZone(ZoneId.of("Europe/London"));
            assertEquals("Europe/London", dateTime.getZone().getId());
            assertEquals("Monday, 13 July 2009 23:31:30 o'clock BST", formatter.format(dateTime));
            assertEquals(1247524290000L, dateTime.toInstant().toEpochMilli());
            
            Date date = Date.from(dateTime.toInstant());
            assertEquals(1247524290000L, date.getTime());
        }
    }
}
