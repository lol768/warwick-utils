package uk.ac.warwick.util.termdates;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.joda.time.Weeks;
import org.junit.Test;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.core.jodatime.DateTimeUtils;
import uk.ac.warwick.util.termdates.Term.TermType;

public final class TermFactoryImplTest {

    @Test
    public void constructor() throws Exception {
        TermFactoryImpl bean = new TermFactoryImpl();

        List<Term> dates = bean.getTermDates();

        Term thirdTerm = dates.get(2);
        DateTime thirdTermStart = thirdTerm.getStartDate();
        DateTime april23rd = new DateTime().withDate(2007, DateTimeConstants.APRIL, 23);

        assertTrue(DateTimeUtils.isSameDay(thirdTermStart, april23rd));

        assertEquals(1, thirdTerm.getWeekNumber(april23rd));
    }
    
    @Test
    public void data() throws Exception {
        for (Term term : new TermFactoryImpl().getTermDates()) {
            assertTrue("Term ends before it finishes (term starting "+term.getStartDate()+")", term.getStartDate().isBefore(term.getEndDate()));
            // This usually evaluates to 9.
            int weeks = new Interval(term.getStartDate(), term.getEndDate()).toPeriod(PeriodType.weeks()).getWeeks();
            assertTrue( "Term has a weird length",  weeks >= 9 && weeks <= 10 );
        }
    }
    
    @Test
    public void enoughDates() throws Exception {
        final int futureTerms = 6;
        TermFactoryImpl factory = new TermFactoryImpl();
        DateTime d = new DateTime().withMonthOfYear(7);
        int i=0;
        try {
            Term term = factory.getTermFromDate(d);
            for (i=1; i<futureTerms; i++) {
                term = factory.getNextTerm(term);
            }
        } catch (TermNotFoundException e) {
            fail("Expected at least " + futureTerms + " future terms in data, but only got "+i+". Update the terms file!");
        }
    }
    
    @Test
    public void getAcademicWeeksForYear() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        List<Pair<Integer, Interval>> weeks = factory.getAcademicWeeksForYear(new DateTime().withDate(2007, DateTimeConstants.APRIL, 23));
        assertEquals(53, weeks.size());
        
        /*
         * 021006;091206;a
         * 080107;170307;sp
         * 230407;300607;su
         */
        
        Pair<Integer, Interval> week1 = weeks.get(0);
        assertEquals(1, week1.getLeft().intValue());
        assertEquals(
            new Interval(
                new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 
                new DateMidnight(2006, DateTimeConstants.OCTOBER, 9)
            ), 
            week1.getRight()
        );
        
        Pair<Integer, Interval> week10 = weeks.get(9);
        assertEquals(10, week10.getLeft().intValue());
        assertEquals(
            new Interval(
                new DateMidnight(2006, DateTimeConstants.DECEMBER, 4), 
                new DateMidnight(2006, DateTimeConstants.DECEMBER, 11)
            ), 
            week10.getRight()
        );
        
        Pair<Integer, Interval> week20 = weeks.get(19);
        assertEquals(20, week20.getLeft().intValue());
        assertEquals(
            new Interval(
                new DateMidnight(2007, DateTimeConstants.FEBRUARY, 12), 
                new DateMidnight(2007, DateTimeConstants.FEBRUARY, 19)
            ), 
            week20.getRight()
        );
        
        Pair<Integer, Interval> week30 = weeks.get(29);
        assertEquals(30, week30.getLeft().intValue());
        assertEquals(
            new Interval(
                new DateMidnight(2007, DateTimeConstants.APRIL, 23), 
                new DateMidnight(2007, DateTimeConstants.APRIL, 30)
            ), 
            week30.getRight()
        );
        
        Pair<Integer, Interval> week52 = weeks.get(51);
        assertEquals(52, week52.getLeft().intValue());
        assertEquals(
            new Interval(
                new DateMidnight(2007, DateTimeConstants.SEPTEMBER, 24), 
                new DateMidnight(2007, DateTimeConstants.OCTOBER, 1)
            ), 
            week52.getRight()
        );
    }
    
    @Test
    public void getAcademicWeek() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        
        Interval week1 = new Interval(
            new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 
            new DateMidnight(2006, DateTimeConstants.OCTOBER, 9)
        );
        assertEquals(
            week1, factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 1)
        );
        assertEquals(
            week1, factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2).plusMonths(5), 1)
        );
        
        try {
            factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2).plusYears(100), 1);
            fail("Should have exceptioned");
        } catch (TermNotFoundException e) {
            // expected
        }
        
        assertEquals(
            new Interval(
                new DateMidnight(2006, DateTimeConstants.DECEMBER, 4), 
                new DateMidnight(2006, DateTimeConstants.DECEMBER, 11)
            ), 
            factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 10)
        );
        
        assertEquals(
            new Interval(
                new DateMidnight(2007, DateTimeConstants.FEBRUARY, 12), 
                new DateMidnight(2007, DateTimeConstants.FEBRUARY, 19)
            ), 
            factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 20)
        );
        
        assertEquals(
            new Interval(
                new DateMidnight(2007, DateTimeConstants.APRIL, 23), 
                new DateMidnight(2007, DateTimeConstants.APRIL, 30)
            ), 
            factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 30)
        );
        
        assertEquals(
            new Interval(
                new DateMidnight(2007, DateTimeConstants.SEPTEMBER, 24), 
                new DateMidnight(2007, DateTimeConstants.OCTOBER, 1)
            ), 
            factory.getAcademicWeek(new DateMidnight(2006, DateTimeConstants.OCTOBER, 2), 52)
        );
    }
    
    @Test
    public void sbtwo3948() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        
        DateTime monday = new DateMidnight(2011, DateTimeConstants.APRIL, 25).toDateTime();
        
        // This should be week 1 of the Summer term
        Term term = factory.getTermFromDate(monday);
        
        assertEquals(new DateMidnight(2011, DateTimeConstants.APRIL, 27), term.getStartDate());
        assertEquals(new DateMidnight(2011, DateTimeConstants.JULY, 2), term.getEndDate());
        assertEquals(TermType.summer, term.getTermType());
        
        assertEquals(1, term.getWeekNumber(monday));
        assertEquals(30, term.getAcademicWeekNumber(monday));
        assertEquals(21, term.getCumulativeWeekNumber(monday));
        
        assertEquals(1, term.getWeekNumber(monday.withDayOfWeek(DateTimeConstants.SATURDAY)));
        assertEquals(30, term.getAcademicWeekNumber(monday.withDayOfWeek(DateTimeConstants.SATURDAY)));
        assertEquals(21, term.getCumulativeWeekNumber(monday.withDayOfWeek(DateTimeConstants.SATURDAY)));
        
        assertEquals(2, term.getWeekNumber(monday.plusWeeks(1)));
        assertEquals(31, term.getAcademicWeekNumber(monday.plusWeeks(1)));
        assertEquals(22, term.getCumulativeWeekNumber(monday.plusWeeks(1)));
    }
    
    @Test
    public void sanity() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        List<Term> terms = factory.getTermDates();
        
        Term lastTerm = null;
        for (Iterator<Term> itr = terms.iterator(); itr.hasNext();) {
            Term term = itr.next();
            
            if (lastTerm != null) {
                assertTrue(term.getStartDate().isAfter(lastTerm.getEndDate()));
                
                switch (lastTerm.getTermType()) {
                    case autumn:
                        assertEquals(TermType.spring, term.getTermType());
                        break;
                    case spring:
                        assertEquals(TermType.summer, term.getTermType());
                        break;
                    case summer:
                        assertEquals(TermType.autumn, term.getTermType());
                        break;
                }
            } else {
                assertEquals(TermType.autumn, term.getTermType());
            }
            
            assertTrue(term.getStartDate().isBefore(term.getEndDate()));
            
            DateTime actualEndDate = term.getEndDate();
            while (actualEndDate.getDayOfWeek() != term.getStartDate().getDayOfWeek()) {
                actualEndDate = actualEndDate.plusDays(1);
            }
            
            assertEquals(10, Weeks.weeksBetween(term.getStartDate(), actualEndDate).getWeeks());
            
            lastTerm = term;
        }
    }

}
