package uk.ac.warwick.util.termdates.legacy;

import org.junit.Test;
import uk.ac.warwick.util.termdates.legacy.Term.TermType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

public class TermImplTest {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");

    @Test
    public void getWeekNumberIsNotDependantOnMilliseconds() throws Exception {
        /**
         * Bump the term start time a few milliseconds into the future, to
         * test that the method isn't dependent on small changes in time.
         */
        LocalDate start = LocalDate.parse("230407", DATE_FORMATTER);
        LocalDate end = LocalDate.parse("300607", DATE_FORMATTER);
        
        LocalDate firstMonday = LocalDate.of(2007, Month.APRIL, 23);
        LocalDate secondMonday = LocalDate.of(2007, Month.APRIL, 30);
        LocalDate happyMondays = LocalDate.of(2007, Month.MAY, 14);
        
        TermImpl term = new TermImpl(null, start, end, TermType.summer);
        
        assertEquals(1, term.getWeekNumber(firstMonday));
        assertEquals(2, term.getWeekNumber(secondMonday));
        assertEquals(4, term.getWeekNumber(happyMondays));
        
        //put this one ahead and see if it still works, just for fun
        assertEquals(1, term.getWeekNumber(firstMonday.atStartOfDay().plus(200, ChronoUnit.MILLIS)));
        assertEquals(2, term.getWeekNumber(secondMonday.atStartOfDay().plus(200, ChronoUnit.MILLIS)));
    }
    
    @Test
    public void getAcademicWeekNumber() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        
        // Check for 2009/10
        LocalDateTime week1start = LocalDateTime.of(2009, Month.OCTOBER, 5, 0, 0, 0, 0);
        LocalDateTime week1 = LocalDateTime.of(2009, Month.OCTOBER, 7, 16, 39, 10, 0);
        LocalDateTime week7 = LocalDateTime.of(2009, Month.NOVEMBER, 17, 16, 39, 10, 0);
        LocalDateTime week11 = LocalDateTime.of(2009, Month.DECEMBER, 14, 16, 39, 10, 0);
        LocalDateTime week12 = LocalDateTime.of(2009, Month.DECEMBER, 22, 16, 39, 10, 0);
        LocalDateTime week14 = LocalDateTime.of(2010, Month.JANUARY, 4, 16, 39, 10, 0);
        LocalDateTime week23 = LocalDateTime.of(2010, Month.MARCH, 10, 16, 39, 10, 0);
        LocalDateTime week28 = LocalDateTime.of(2010, Month.APRIL, 15, 16, 39, 10, 0);
        LocalDateTime week39 = LocalDateTime.of(2010, Month.JUNE, 30, 16, 39, 10, 0);
        LocalDateTime week52 = LocalDateTime.of(2010, Month.OCTOBER, 1, 16, 39, 10, 0);
        LocalDateTime week52end = LocalDateTime.of(2010, Month.OCTOBER, 4, 0, 0, 0, 0).minus(1, ChronoUnit.MILLIS);
        
        assertEquals(1, factory.getTermFromDate(week1start).getWeekNumber(week1start));
        assertEquals(1, factory.getTermFromDate(week1start).getAcademicWeekNumber(week1start));
        assertEquals(1, factory.getTermFromDate(week1start).getCumulativeWeekNumber(week1start));
        
        assertEquals(1, factory.getTermFromDate(week1).getWeekNumber(week1));
        assertEquals(1, factory.getTermFromDate(week1).getAcademicWeekNumber(week1));
        assertEquals(1, factory.getTermFromDate(week1).getCumulativeWeekNumber(week1));
        
        assertEquals(7, factory.getTermFromDate(week7).getWeekNumber(week7));
        assertEquals(7, factory.getTermFromDate(week7).getAcademicWeekNumber(week7));
        assertEquals(7, factory.getTermFromDate(week7).getCumulativeWeekNumber(week7));
        
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week11).getWeekNumber(week11));
        assertEquals(11, factory.getTermFromDate(week11).getAcademicWeekNumber(week11));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week11).getCumulativeWeekNumber(week11));
        
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week12).getWeekNumber(week12));
        assertEquals(12, factory.getTermFromDate(week12).getAcademicWeekNumber(week12));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week12).getCumulativeWeekNumber(week12));
        
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week14).getWeekNumber(week14));
        assertEquals(14, factory.getTermFromDate(week14).getAcademicWeekNumber(week14));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week14).getCumulativeWeekNumber(week14));
        
        assertEquals(9, factory.getTermFromDate(week23).getWeekNumber(week23));
        assertEquals(23, factory.getTermFromDate(week23).getAcademicWeekNumber(week23));
        assertEquals(19, factory.getTermFromDate(week23).getCumulativeWeekNumber(week23));
        
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week28).getWeekNumber(week28));
        assertEquals(28, factory.getTermFromDate(week28).getAcademicWeekNumber(week28));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week28).getCumulativeWeekNumber(week28));
        
        assertEquals(10, factory.getTermFromDate(week39).getWeekNumber(week39));
        assertEquals(39, factory.getTermFromDate(week39).getAcademicWeekNumber(week39));
        assertEquals(30, factory.getTermFromDate(week39).getCumulativeWeekNumber(week39));
        
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week52).getWeekNumber(week52));
        assertEquals(52, factory.getTermFromDate(week52).getAcademicWeekNumber(week52));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week52).getCumulativeWeekNumber(week52));
        
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week52end).getWeekNumber(week52end));
        assertEquals(52, factory.getTermFromDate(week52end).getAcademicWeekNumber(week52end));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week52end).getCumulativeWeekNumber(week52end));
        
        // 2008/2009 had a week 53
        LocalDateTime week53 = week1start.minus(1, ChronoUnit.MILLIS);
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week53).getWeekNumber(week53));
        assertEquals(53, factory.getTermFromDate(week53).getAcademicWeekNumber(week53));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week53).getCumulativeWeekNumber(week53));
    }

}
