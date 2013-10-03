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
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
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
	public LocalDate datePlusWorkingDays(LocalDate start, int numWorkingDays){
		int daysAdded = 0;
		LocalDate result = start;
		while(daysAdded < numWorkingDays){
			result = result.plusDays(1);
			if(result.getDayOfWeek() <= DateTimeConstants.FRIDAY && !holidayDates.contains(result)){
				daysAdded++;
			}
		}
		return result;
	}

	@Override
	public int getNumWorkingDays(LocalDate first, LocalDate last) {
	    final boolean isNegative;
	    final LocalDate start, end;
	    
	    if (last.isBefore(first)) {
	        // Reverse the order but remember to negate it later
	        start = last;
	        end = first;
	        isNegative = true;
	    } else {
	        start = first;
	        end = last;
	        isNegative = false;
	    }

		int numDays = 0;
		LocalDate temp = start;

		while(temp.isBefore(end) || temp.isEqual(end)){
			// if is weekend or holiday ignore
			if (temp.getDayOfWeek() <= DateTimeConstants.FRIDAY && !holidayDates.contains(temp)){
				numDays++;
			}
			temp = temp.plusDays(1);
		}
		
		return (isNegative) ? -numDays : numDays;
	}

	public Set<LocalDate> getHolidayDates(){
		return holidayDates;
	}

	public void setHolidayDates(Set<LocalDate> holidayDates){
		this.holidayDates = holidayDates;
	}
}
