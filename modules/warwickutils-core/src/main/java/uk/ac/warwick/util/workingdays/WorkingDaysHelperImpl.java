package uk.ac.warwick.util.workingdays;

import uk.ac.warwick.util.core.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class WorkingDaysHelperImpl implements WorkingDaysHelper {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");

	private Set<LocalDate> holidayDates = new HashSet<>();

	public WorkingDaysHelperImpl() throws IOException {
		String source = StringUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("workingdays.txt")));

		for (StringTokenizer st = new StringTokenizer(source, "\n"); st.hasMoreTokens();) {
			String line = st.nextToken().trim();
			LocalDate holiday = LocalDate.parse(line, DATE_FORMATTER);
			holidayDates.add(holiday);
		}
	}

	@Override
	public LocalDate datePlusWorkingDays(LocalDate start, int numWorkingDays){
		int daysAdded = 0;
		LocalDate result = start;
		while(daysAdded < numWorkingDays){
			result = result.plusDays(1);
			if(result.getDayOfWeek().compareTo(DayOfWeek.FRIDAY) <= 0 && !holidayDates.contains(result)){
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
			if (temp.getDayOfWeek().compareTo(DayOfWeek.FRIDAY) <= 0 && !holidayDates.contains(temp)){
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
