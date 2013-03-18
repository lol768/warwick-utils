package uk.ac.warwick.util.workingdays;

import org.joda.time.LocalDate;

import java.util.Set;

public interface WorkingDaysHelper {
	public int getNumWorkingDays(LocalDate start, LocalDate end);
	public Set<LocalDate> getHolidayDates();
	public void setHolidayDates(Set<LocalDate> holidayDates);
}
