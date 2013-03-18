package uk.ac.warwick.util.workingdays;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class WorkingDaysHelperImpl implements WorkingDaysHelper {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("ddMMyy");

	private Set<LocalDate> holidayDates = new HashSet<LocalDate>();

	public WorkingDaysHelperImpl() throws IOException {
		String source = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("workingdays.txt")));

		for (StringTokenizer st = new StringTokenizer(source, "\n"); st.hasMoreTokens();) {
			String line = st.nextToken().trim();
			DateTime holiday = DATE_FORMATTER.parseDateTime(line);
			holidayDates.add(holiday.toLocalDate());
		}
	}

	@Override
	public int getNumWorkingDays(LocalDate start, LocalDate end) {

		if(end.isBefore(start))
			throw new IllegalStateException("End date is before start date.");

		int numDays = 0;
		LocalDate temp = start;

		while(temp.isBefore(end) || temp.isEqual(end)){
			// if is weekend or holiday ignore
			if(temp.getDayOfWeek() <= DateTimeConstants.FRIDAY && !holidayDates.contains(temp)){
				numDays++;
			}
			temp = temp.plusDays(1);
		}
		return numDays;
	}

	public Set<LocalDate> getHolidayDates(){
		return holidayDates;
	}

	public void setHolidayDates(Set<LocalDate> holidayDates){
		this.holidayDates = holidayDates;
	}
}
