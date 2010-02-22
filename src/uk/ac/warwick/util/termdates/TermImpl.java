package uk.ac.warwick.util.termdates;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.MutableDateTime;
import org.joda.time.base.BaseDateTime;
import org.springframework.beans.factory.annotation.Configurable;

import uk.ac.warwick.util.core.jodatime.DateTimeUtils;

@Configurable
public final class TermImpl implements Term {
    
    private transient TermFactory termFactory;

    private DateTime startDate;

    private DateTime endDate;

    private TermType termType;

    public TermImpl(final DateTime start, final DateTime end, final TermType type) {
        this.startDate = start;
        this.endDate = end;
        this.termType = type;
    }

    public DateTime getEndDate() {
        return endDate;
    }
    
    public DateTime getStartDate() {
        return startDate;
    }
    
    public TermType getTermType() {
        return termType;
    }

    public String getTermTypeAsString() {
        String result;
        switch (termType) {
            case autumn:
                result = "Autumn";
                break;
            case spring:
                result = "Spring";
                break;
            case summer:
                result = "Summer";
                break;
            default:
                throw new IllegalStateException("");
        }
        return result;
    }

    public int getWeekNumber(final BaseDateTime dt) {
        boolean firstDayOfTerm = DateTimeUtils.isSameDay(dt, startDate);

        if (dt.isBefore(startDate) && !firstDayOfTerm) {
            return WEEK_NUMBER_BEFORE_START;
        }

        int result = WEEK_NUMBER_AFTER_END;

        MutableDateTime theWeek = startDate.toMutableDateTime();

        for (int week = 1; week <= NUMBER_OF_WEEKS_IN_TERM; week++) {
            theWeek.addWeeks(1);
            if (dt.isBefore(theWeek) && !DateTimeUtils.isSameDay(dt, theWeek)) {
                result = week;
                break;
            }
        }

        return result;
    }
    
    public int getCumulativeWeekNumber(final BaseDateTime dt) {
        int weekNumber = getWeekNumber(dt);
        if (weekNumber > 0) {
            switch (termType) {
                case autumn:
                    break;
                case summer:
                    weekNumber += NUMBER_OF_WEEKS_IN_TERM;
                    // Intentional fall-through - for summer term we want to add the value twice
                case spring:
                    weekNumber += NUMBER_OF_WEEKS_IN_TERM;
                    break;
                default:
                    throw new IllegalArgumentException("invalid term type: " + termType);
            }
        }
        
        return weekNumber;
    }
    
    public int getAcademicWeekNumber(BaseDateTime dt) {
        // Get the start and end dates for this academic year. Roll back to the
        // autumn term and get the start date, then go to the NEXT autumn term
        // and get the start date less a day.
        MutableDateTime start = null;
        DateTime end = null;
        int result = 0;
        
        try {
            start = getAutumnTermStartDate(this, dt);
        } catch (TermNotFoundException e) {
            result = WEEK_NUMBER_BEFORE_START;
        }
        
        try {
            end = getBeforeNextAutumnTermDate(this);
        } catch (TermNotFoundException e) {
            result = WEEK_NUMBER_AFTER_END;
        }
        
        if (result != 0 || start == null || end == null) {
            return result;
        }
        
        boolean firstDayOfTerm = DateTimeUtils.isSameDay(dt, start);

        if (dt.isBefore(start) && !firstDayOfTerm) {
            return WEEK_NUMBER_BEFORE_START;
        }

        result = WEEK_NUMBER_AFTER_END;
        
        for (int week = 1; week <= MAX_NUMBER_OF_WEEKS_IN_ACADEMIC_YEAR; week++) {
            start.addWeeks(1);
            if (dt.isBefore(start) && !DateTimeUtils.isSameDay(dt, start)) {
                result = week;
                break;
            }
        }
        
        return result;
    }

    private MutableDateTime getAutumnTermStartDate(Term t, BaseDateTime dt) throws TermNotFoundException {
        MutableDateTime start;
        Term autumnTerm = t;
        while (autumnTerm.getStartDate().isAfter(dt) || autumnTerm.getTermType() != TermType.autumn) {
            autumnTerm = termFactory.getPreviousTerm(autumnTerm);
        }
        
        start = autumnTerm.getStartDate().toMutableDateTime();
        return start;
    }

    private DateTime getBeforeNextAutumnTermDate(Term t) throws TermNotFoundException {
        DateTime end;
        Term nextAutumnTerm = t;
        while (nextAutumnTerm.getStartDate().equals(startDate) || nextAutumnTerm.getTermType() != TermType.autumn) {
            nextAutumnTerm = termFactory.getNextTerm(nextAutumnTerm);
        }
        
        end = nextAutumnTerm.getStartDate().minusWeeks(1).withDayOfWeek(DateTimeConstants.SUNDAY);
        return end;
    }

    public void setTermFactory(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

}
