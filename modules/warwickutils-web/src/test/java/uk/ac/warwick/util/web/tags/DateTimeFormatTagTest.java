package uk.ac.warwick.util.web.tags;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public final class DateTimeFormatTagTest {
	
	@Test
	public void itWorksJSP() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		tag.setPattern("EEEE, dd MMMM ''yy");
		tag.setValue(LocalDateTime.of(2009, Month.JANUARY, 15, 15, 30, 15, 0));
		
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
		tag.setValue(LocalDateTime.of(2009, Month.JANUARY, 15, 15, 30, 15, 0));
		
		tag.doEndTag();
		fail("Should have thrown exception");
	}
	
	@Test
	public void itWorksFreemarker() throws Exception {
		DateTimeFormatTag tag = new DateTimeFormatTag();
		
		StringModel model = new StringModel(LocalDateTime.of(2009, Month.JANUARY, 15, 15, 30, 15, 0), BeansWrapper.getDefaultInstance());
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
		
		StringModel model = new StringModel(LocalDateTime.of(2009, Month.JANUARY, 15, 15, 30, 15, 0), BeansWrapper.getDefaultInstance());
		tag.exec(Arrays.asList(model));
		
		fail("Should have thrown exception");
	}

}
