package uk.ac.warwick.util.content.freemarker;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;

public final class DateTimeFreemarkerObjectWrapperTest {
    
    private TimeZone defaultTz;
    private Locale defaultLocale;
    
    @Before
    public void setUp() throws Exception {
        this.defaultTz = TimeZone.getDefault();
        this.defaultLocale = Locale.getDefault();
    }
    
    @After
    public void tearDown() throws Exception {
        TimeZone.setDefault(defaultTz);
        Locale.setDefault(defaultLocale);
    }
    
    @Test 
    public void jodaTimeWrapping() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        Locale.setDefault(Locale.UK);
        
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(getClass(), "");
        configuration.setObjectWrapper(new DateTimeFreemarkerObjectWrapper());

        Template template = configuration.getTemplate("joda-test.ftl");
        
        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("greeting", "hello");
        
        StringWriter out = new StringWriter();
        rootMap.put("d", new Date(1234567890000l));
        template.process(rootMap, out);
        assertEquals("start;13 February 2009 23:31:30 GMT;end", out.toString());
        
        out = new StringWriter();
        rootMap.put("d", new DateTime(1234567890000l));
        template.process(rootMap, out);
        assertEquals("start;13 February 2009 23:31:30 GMT;end", out.toString());
        
        out = new StringWriter();
        DateTime dateTime = new DateTime(2009, DateTimeConstants.JULY, 1, 1, 2, 3, 4);
        assertEquals("Europe/London", dateTime.getZone().getID());
        rootMap.put("d", dateTime);
        template.process(rootMap, out);
        
        assertEquals("start;01 July 2009 01:02:03 BST;end", out.toString());
    }
    
    @Test
    public void jodaToDateConversion() throws Exception {
        DateTimeFormatter formatter = DateTimeFormat.forStyle("FF").withLocale(Locale.UK);
        
        {
            DateTime dateTime = new DateTime(2009, DateTimeConstants.FEBRUARY, 13, 23, 31, 30, 0);
            assertEquals("Europe/London", dateTime.getZone().getID());
            assertEquals("Friday, 13 February 2009 23:31:30 o'clock GMT", formatter.print(dateTime));
            assertEquals(1234567890000l, dateTime.getMillis());
            
            Date date = new Date(dateTime.getMillis());
            assertEquals(1234567890000l, date.getTime());
            
            DateTime dateTime2 = new DateTime(date);
            assertEquals("Europe/London", dateTime2.getZone().getID());
            assertEquals("Friday, 13 February 2009 23:31:30 o'clock GMT", formatter.print(dateTime2));
            assertEquals(1234567890000l, dateTime2.getMillis());
        }
        
        // now let's try a BST one
        {
            DateTime dateTime = new DateTime(2009, DateTimeConstants.JULY, 13, 23, 31, 30, 0);
            assertEquals("Europe/London", dateTime.getZone().getID());
            assertEquals("Monday, 13 July 2009 23:31:30 o'clock BST", formatter.print(dateTime));
            assertEquals(1247524290000L, dateTime.getMillis());
            
            Date date = new Date(dateTime.getMillis());
            assertEquals(1247524290000L, date.getTime());
            
            DateTime dateTime2 = new DateTime(date);
            assertEquals("Europe/London", dateTime2.getZone().getID());
            assertEquals("Monday, 13 July 2009 23:31:30 o'clock BST", formatter.print(dateTime2));
            assertEquals(1247524290000L, dateTime2.getMillis());
        }
    }
}
