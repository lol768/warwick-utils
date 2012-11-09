package uk.ac.warwick.util.web.tags;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

public final class DateTimeFormatTagTest {
	
	@Test
	public void itWorksJSP() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		tag.setPattern("EEEE, dd MMMM ''yy");
		tag.setValue(new DateTime(2009, DateTimeConstants.JANUARY, 15, 15, 30, 15, 0));
		
		assertEquals("Thursday, 15 January '09", tag.getFormattedDate());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionOnNoDateTimeJSP() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		tag.setPattern("EEEE, dd MMMM ''yy");
		
		tag.doEndTag();
		fail("Should have thrown exception");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void throwsExceptionOnNoFormatJSP() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		tag.setValue(new DateTime(2009, DateTimeConstants.JANUARY, 15, 15, 30, 15, 0));
		
		tag.doEndTag();
		fail("Should have thrown exception");
	}
	
	@Test
	public void itWorksFreemarker() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		
		StringModel model = new StringModel(new DateTime(2009, DateTimeConstants.JANUARY, 15, 15, 30, 15, 0), BeansWrapper.getDefaultInstance());
		List<?> arguments = Arrays.asList(new SimpleScalar("EEEE, dd MMMM ''yy"), model);
		
		assertEquals("Thursday, 15 January '09", tag.exec(arguments));
	}
	
	@Test(expected=TemplateModelException.class)
	public void throwsExceptionOnNoDateTimeFreemarker() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		tag.exec(Arrays.asList(new SimpleScalar("EEEE, dd MMMM ''yy")));
		
		fail("Should have thrown exception");
	}
	
	@Test(expected=TemplateModelException.class)
	public void throwsExceptionOnNoFormatFreemarker() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		
		StringModel model = new StringModel(new DateTime(2009, DateTimeConstants.JANUARY, 15, 15, 30, 15, 0), BeansWrapper.getDefaultInstance());
		tag.exec(Arrays.asList(model));
		
		fail("Should have thrown exception");
	}

}
