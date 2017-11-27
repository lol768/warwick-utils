package uk.ac.warwick.util.workingdays;

import java.time.LocalDate;
import java.util.Set;

public interface WorkingDaysHelper {
	LocalDate datePlusWorkingDays(LocalDate start, int numWorkingDays);
	int getNumWorkingDays(LocalDate start, LocalDate end);
	Set<LocalDate> getHolidayDates();
	void setHolidayDates(Set<LocalDate> holidayDates);
}
