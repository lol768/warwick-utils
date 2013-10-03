package uk.ac.warwick.util.workingdays;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class WorkingDaysHelperTest {

	@Test
	public void constructor() throws Exception {
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();
		Set<LocalDate> holidayDates = bean.getHolidayDates();

		DateTime xmas = new DateTime().withDate(2012, DateTimeConstants.DECEMBER, 25);
		assertTrue(holidayDates.contains(xmas.toLocalDate()));

		DateTime mayDay = new DateTime().withDate(2013, DateTimeConstants.MAY, 06);
		assertTrue(holidayDates.contains(mayDay.toLocalDate()));
	}


	@Test
	public void testRange() throws Exception {
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();

		DateTime start = new DateTime().withDate(2012, DateTimeConstants.DECEMBER, 01);
		DateTime end = new DateTime().withDate(2013, DateTimeConstants.JANUARY, 31);
		assertEquals(37, bean.getNumWorkingDays(start.toLocalDate(), end.toLocalDate()));

		start = new DateTime().withDate(2013, DateTimeConstants.MAY, 01);
		end = new DateTime().withDate(2013, DateTimeConstants.MAY, 31);
		assertEquals(21, bean.getNumWorkingDays(start.toLocalDate(), end.toLocalDate()));
	}
	
	@Test
	public void testNegativeRange() throws Exception {
	    WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();

        DateTime end = new DateTime().withDate(2012, DateTimeConstants.DECEMBER, 01);
        DateTime start = new DateTime().withDate(2013, DateTimeConstants.JANUARY, 31);
        assertEquals(-37, bean.getNumWorkingDays(start.toLocalDate(), end.toLocalDate()));

        end = new DateTime().withDate(2013, DateTimeConstants.MAY, 01);
        start = new DateTime().withDate(2013, DateTimeConstants.MAY, 31);
        assertEquals(-21, bean.getNumWorkingDays(start.toLocalDate(), end.toLocalDate()));
	}

	@Test
	public void addWorkingDays() throws Exception {
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();
		DateTime start = new DateTime().withDate(2012, DateTimeConstants.DECEMBER, 10);
		assertEquals(new DateTime().withDate(2013, DateTimeConstants.JANUARY, 16).toLocalDate(), bean.datePlusWorkingDays(start.toLocalDate(), 20));
	}

	@Test
	public void testHasFutureHolidays() throws Exception {
		// tests if any holiday date from the text file is from at least six months in the future.
		// if this fails then add new dates to the text file!
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();
		Set<LocalDate> holidayDates = bean.getHolidayDates();

		DateTime nextYear = new DateTime().plusMonths(6);
		LocalDate nextYearLocal = nextYear.toLocalDate();
		LocalDate newestFound = null;

		Iterator<LocalDate> i = holidayDates.iterator();
		boolean result = false;
		while (!result && i.hasNext()){
			LocalDate next = (LocalDate) i.next();
			if (newestFound == null || newestFound.isBefore(next)){
				newestFound = next;
			}
			result = next.isAfter(nextYearLocal);
		}

		if (!result)
			fail("No holiday dates found after 6 months. The newest date supplied is "+newestFound);
	}
}
