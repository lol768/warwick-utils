package uk.ac.warwick.util.termdates;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import uk.ac.warwick.util.termdates.Term.TermType;

public class TermImplTest {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("ddMMyy");

    @Test
    public void getWeekNumberIsNotDependantOnMilliseconds() throws Exception {
        /**
         * Bump the term start time a few milliseconds into the future, to
         * test that the method isn't dependent on small changes in time.
         */
        DateTime start = DATE_FORMATTER.parseDateTime("230407").plusMillis(50);
        DateTime end = DATE_FORMATTER.parseDateTime("300607");
        
        DateTime firstMonday = new DateTime().withDate(2007,DateTimeConstants.APRIL,23);
        DateTime secondMonday = new DateTime().withDate(2007,DateTimeConstants.APRIL,30);
        DateTime happyMondays = new DateTime().withDate(2007,DateTimeConstants.MAY,14);
        
        TermImpl term = new TermImpl(null, start, end, TermType.summer);
        
        assertEquals(1, term.getWeekNumber(firstMonday));
        assertEquals(2, term.getWeekNumber(secondMonday));
        assertEquals(4, term.getWeekNumber(happyMondays));
        
        //put this one ahead and see if it still works, just for fun
        firstMonday = firstMonday.plusMillis(200);
        secondMonday = secondMonday.plusMillis(200);
        
        assertEquals(1, term.getWeekNumber(firstMonday));
        assertEquals(2, term.getWeekNumber(secondMonday));
    }
    
    @Test
    public void getAcademicWeekNumber() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        
        // Check for 2009/10
        DateTime week1start = new DateTime(2009, DateTimeConstants.OCTOBER, 5, 0, 0, 0, 0);
        DateTime week1 = new DateTime(2009, DateTimeConstants.OCTOBER, 7, 16, 39, 10, 0);
        DateTime week7 = new DateTime(2009, DateTimeConstants.NOVEMBER, 17, 16, 39, 10, 0);
        DateTime week11 = new DateTime(2009, DateTimeConstants.DECEMBER, 14, 16, 39, 10, 0);
        DateTime week12 = new DateTime(2009, DateTimeConstants.DECEMBER, 22, 16, 39, 10, 0);
        DateTime week14 = new DateTime(2010, DateTimeConstants.JANUARY, 4, 16, 39, 10, 0);
        DateTime week23 = new DateTime(2010, DateTimeConstants.MARCH, 10, 16, 39, 10, 0);
        DateTime week28 = new DateTime(2010, DateTimeConstants.APRIL, 15, 16, 39, 10, 0);
        DateTime week39 = new DateTime(2010, DateTimeConstants.JUNE, 30, 16, 39, 10, 0);
        DateTime week52 = new DateTime(2010, DateTimeConstants.OCTOBER, 1, 16, 39, 10, 0);
        DateTime week52end = new DateTime(2010, DateTimeConstants.OCTOBER, 4, 0, 0, 0, 0).minusMillis(1);
        
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
        DateTime week53 = week1start.minusMillis(1); 
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week53).getWeekNumber(week53));
        assertEquals(53, factory.getTermFromDate(week53).getAcademicWeekNumber(week53));
        assertEquals(Term.WEEK_NUMBER_BEFORE_START, factory.getTermFromDate(week53).getCumulativeWeekNumber(week53));
    }

}
