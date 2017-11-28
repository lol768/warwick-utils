package uk.ac.warwick.util.termdates.legacy;

import uk.ac.warwick.util.core.DateTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.Temporal;

/**
 * @deprecated Use {@link uk.ac.warwick.util.termdates.service.TermDatesService}
 */
public final class TermImpl implements Term {
    
    private final TermFactory termFactory;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final TermType termType;

    public TermImpl(final TermFactory factory, final LocalDate start, final LocalDate end, final TermType type) {
        this.termFactory = factory;
        this.startDate = start;
        this.endDate = end;
        this.termType = type;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    
    public LocalDate getStartDate() {
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

    public int getWeekNumber(final Temporal temporal) {
        LocalDate dt = LocalDate.from(temporal);
        LocalDate startOfFirstWeek = startDate.with(DayOfWeek.MONDAY);
        
        boolean firstDayOfTerm = DateTimeUtils.isSameDay(dt, startOfFirstWeek);

        if (dt.isBefore(startOfFirstWeek) && !firstDayOfTerm) {
            return WEEK_NUMBER_BEFORE_START;
        }

        int result = WEEK_NUMBER_AFTER_END;

        LocalDate theWeek = startOfFirstWeek;

        for (int week = 1; week <= NUMBER_OF_WEEKS_IN_TERM; week++) {
            theWeek = theWeek.plusWeeks(1);
            if (dt.isBefore(theWeek) && !DateTimeUtils.isSameDay(dt, theWeek)) {
                result = week;
                break;
            }
        }

        return result;
    }
    
    public int getCumulativeWeekNumber(final Temporal dt) {
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
    
    public int getAcademicWeekNumber(Temporal temporal) {
        LocalDate dt = LocalDate.from(temporal);

        // Get the start and end dates for this academic year. Roll back to the
        // autumn term and get the start date, then go to the NEXT autumn term
        // and get the start date less a day.
        LocalDate start = null;
        LocalDate end = null;
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
            start = start.plusWeeks(1);
            if (dt.isBefore(start) && !DateTimeUtils.isSameDay(dt, start)) {
                result = week;
                break;
            }
        }
        
        return result;
    }

    private LocalDate getAutumnTermStartDate(Term t, Temporal dt) throws TermNotFoundException {
        Term autumnTerm = t;
        while (autumnTerm.getStartDate().isAfter(LocalDate.from(dt)) || autumnTerm.getTermType() != TermType.autumn) {
            autumnTerm = termFactory.getPreviousTerm(autumnTerm);
        }
        
        return autumnTerm.getStartDate();
    }

    private LocalDate getBeforeNextAutumnTermDate(Term t) throws TermNotFoundException {
        Term nextAutumnTerm = t;
        while (nextAutumnTerm.getStartDate().equals(startDate) || nextAutumnTerm.getTermType() != TermType.autumn) {
            nextAutumnTerm = termFactory.getNextTerm(nextAutumnTerm);
        }
        
        return nextAutumnTerm.getStartDate().minusWeeks(1).with(DayOfWeek.SUNDAY);
    }

}
