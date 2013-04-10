package uk.ac.warwick.util.workingdays;

import org.joda.time.LocalDate;

import java.util.Set;

public interface WorkingDaysHelper {
	public LocalDate datePlusWorkingDays(LocalDate start, int numWorkingDays);
	public int getNumWorkingDays(LocalDate start, LocalDate end);
	public Set<LocalDate> getHolidayDates();
	public void setHolidayDates(Set<LocalDate> holidayDates);
}
