package uk.ac.warwick.util.workingdays;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

public final class WorkingDaysHelperTest {

	@Test
	public void constructor() throws Exception {
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();
		Set<LocalDate> holidayDates = bean.getHolidayDates();

		LocalDate xmas = LocalDate.of(2012, Month.DECEMBER, 25);
		assertTrue(holidayDates.contains(xmas));

		LocalDate mayDay = LocalDate.of(2013, Month.MAY, 6);
		assertTrue(holidayDates.contains(mayDay));
	}


	@Test
	public void testRange() throws Exception {
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();

		LocalDate start = LocalDate.of(2012, Month.DECEMBER, 1);
		LocalDate end = LocalDate.of(2013, Month.JANUARY, 31);
		assertEquals(37, bean.getNumWorkingDays(start, end));

		start = LocalDate.of(2013, Month.MAY, 1);
		end = LocalDate.of(2013, Month.MAY, 31);
		assertEquals(21, bean.getNumWorkingDays(start, end));
	}
	
	@Test
	public void testNegativeRange() throws Exception {
	    WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();

        LocalDate end = LocalDate.of(2012, Month.DECEMBER, 1);
        LocalDate start = LocalDate.of(2013, Month.JANUARY, 31);
        assertEquals(-37, bean.getNumWorkingDays(start, end));

        end = LocalDate.of(2013, Month.MAY, 1);
        start = LocalDate.of(2013, Month.MAY, 31);
        assertEquals(-21, bean.getNumWorkingDays(start, end));
	}

	@Test
	public void addWorkingDays() throws Exception {
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();
		LocalDate start = LocalDate.of(2012, Month.DECEMBER, 10);
		assertEquals(LocalDate.of(2013, Month.JANUARY, 16), bean.datePlusWorkingDays(start, 20));
	}

	@Test
	public void testHasFutureHolidays() throws Exception {
		// tests if any holiday date from the text file is from at least four months in the future.
		// if this fails then add new dates to the text file!
		WorkingDaysHelperImpl bean = new WorkingDaysHelperImpl();
		Set<LocalDate> holidayDates = bean.getHolidayDates();

		LocalDate nextYear = LocalDate.now().plusMonths(4);
		LocalDate newestFound = null;

		Iterator<LocalDate> i = holidayDates.iterator();
		boolean result = false;
		while (!result && i.hasNext()){
			LocalDate next = i.next();
			if (newestFound == null || newestFound.isBefore(next)){
				newestFound = next;
			}
			result = next.isAfter(nextYear);
		}

		if (!result)
			fail("No holiday dates found after 4 months. The newest date supplied is "+newestFound);
	}
}
