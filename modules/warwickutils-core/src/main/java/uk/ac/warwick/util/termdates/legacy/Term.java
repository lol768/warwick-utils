package uk.ac.warwick.util.termdates.legacy;

import java.time.LocalDate;
import java.time.temporal.Temporal;

/**
 * Utility object used to represent a Term at the University, usually 10 weeks
 * with a start date and an end date.
 * 
 * @author Mat Mannion
 * @deprecated Use {@link uk.ac.warwick.util.termdates.service.TermDatesService}
 */
public interface Term {

    int WEEK_NUMBER_BEFORE_START = -1;

    int WEEK_NUMBER_AFTER_END = -2;

    int NUMBER_OF_WEEKS_IN_TERM = 10;
    
    int MAX_NUMBER_OF_WEEKS_IN_ACADEMIC_YEAR = 53;

    public enum TermType {
        autumn, spring, summer
    }

    LocalDate getStartDate();

    LocalDate getEndDate();

    TermType getTermType();
    
    String getTermTypeAsString();

    /**
     * Get the week number for the date on this term.
     * 
     * @return The week number, starting at 1 for the first week of the term.
     *         Will return -1 for before the start of term and -2 for after the
     *         end of term.
     */
    int getWeekNumber(Temporal dateTime);
    
    /**
     * Gets the cumulative week number - week 1 of term 2 is 11, not 1, and week 1 of term 3 is 21.
     */
    int getCumulativeWeekNumber(Temporal dateTime);

    /**
     * Get the academic week number - starts at 1 for the first week of the
     * autumn term and finishes at 52 on the week before week 1 of next year's
     * autumn term.
     * 
     * @return The academic week number - will be the same for autumn, spring
     *         and summer terms of the same academic year. Will return -1 for
     *         weeks before the start of the academic year, and -2 for weeks
     *         after the end of the academic year.
     */
    int getAcademicWeekNumber(Temporal dateTime);

}
