package uk.ac.warwick.util.workingdays;

import org.joda.time.LocalDate;

public interface WorkingDaysHelper {
	public int getNumWorkingDays(LocalDate start, LocalDate end);
}
