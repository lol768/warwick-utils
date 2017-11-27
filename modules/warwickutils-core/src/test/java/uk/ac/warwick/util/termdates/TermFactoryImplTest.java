package uk.ac.warwick.util.termdates;

import org.junit.Test;
import org.threeten.extra.LocalDateRange;
import org.threeten.extra.Weeks;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.core.DateTimeUtils;
import uk.ac.warwick.util.termdates.Term.TermType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.Assert.*;

public final class TermFactoryImplTest {

    @Test
    public void constructor() throws Exception {
        TermFactoryImpl bean = new TermFactoryImpl();

        List<Term> dates = bean.getTermDates();

        Term thirdTerm = dates.get(2);
        LocalDate thirdTermStart = thirdTerm.getStartDate();
        LocalDate april23rd = LocalDate.of(2007, Month.APRIL, 23);

        assertTrue(DateTimeUtils.isSameDay(thirdTermStart, april23rd));

        assertEquals(1, thirdTerm.getWeekNumber(april23rd));
    }
    
    @Test
    public void data() throws Exception {
        for (Term term : new TermFactoryImpl().getTermDates()) {
            assertTrue("Term ends before it finishes (term starting "+term.getStartDate()+")", term.getStartDate().isBefore(term.getEndDate()));
            // This usually evaluates to 9.
            int weeks = Weeks.between(term.getStartDate(), term.getEndDate()).getAmount();
            assertTrue( "Term has a weird length",  weeks >= 9 && weeks <= 10 );
        }
    }
    
    @Test
    public void enoughDates() throws Exception {
        final int futureTerms = 6;
        TermFactoryImpl factory = new TermFactoryImpl();
        LocalDate d = LocalDate.now().withMonth(7);
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
        List<Pair<Integer, LocalDateRange>> weeks = factory.getAcademicWeeksForYear(LocalDate.of(2007, Month.APRIL, 23));
        assertEquals(52, weeks.size());
        
        /*
         * 021006;091206;a
         * 080107;170307;sp
         * 230407;300607;su
         */
        
        Pair<Integer, LocalDateRange> week1 = weeks.get(0);
        assertEquals(1, week1.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2006, Month.OCTOBER, 2),
                LocalDate.of(2006, Month.OCTOBER, 9)
            ), 
            week1.getRight()
        );
        
        Pair<Integer, LocalDateRange> week10 = weeks.get(9);
        assertEquals(10, week10.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2006, Month.DECEMBER, 4),
                LocalDate.of(2006, Month.DECEMBER, 11)
            ), 
            week10.getRight()
        );
        
        Pair<Integer, LocalDateRange> week20 = weeks.get(19);
        assertEquals(20, week20.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.FEBRUARY, 12),
                LocalDate.of(2007, Month.FEBRUARY, 19)
            ), 
            week20.getRight()
        );
        
        Pair<Integer, LocalDateRange> week30 = weeks.get(29);
        assertEquals(30, week30.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.APRIL, 23),
                LocalDate.of(2007, Month.APRIL, 30)
            ), 
            week30.getRight()
        );
        
        Pair<Integer, LocalDateRange> week52 = weeks.get(51);
        assertEquals(52, week52.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.SEPTEMBER, 24),
                LocalDate.of(2007, Month.OCTOBER, 1)
            ), 
            week52.getRight()
        );
    }

    @Test
    public void getAcademicWeeksForYear2014() throws Exception {
        // 2014/15 is special, it has 53 weeks

        TermFactoryImpl factory = new TermFactoryImpl();
        List<Pair<Integer, LocalDateRange>> weeks = factory.getAcademicWeeksForYear(LocalDate.of(2014, Month.NOVEMBER, 1));
        assertEquals(53, weeks.size());

        /*
         * 290914;061214;a
         * 050115;140315;sp
         * 200415;270615;su
         */

        Pair<Integer, LocalDateRange> week1 = weeks.get(0);
        assertEquals(1, week1.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2014, Month.SEPTEMBER, 29),
                LocalDate.of(2014, Month.OCTOBER, 6)
            ),
            week1.getRight()
        );

        Pair<Integer, LocalDateRange> week53 = weeks.get(52);
        assertEquals(53, week53.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2015, Month.SEPTEMBER, 28),
                LocalDate.of(2015, Month.OCTOBER, 5)
            ),
            week53.getRight()
        );
    }

    @Test
    public void getAcademicWeeksForYear2015() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        List<Pair<Integer, LocalDateRange>> weeks = factory.getAcademicWeeksForYear(LocalDate.of(2015, Month.NOVEMBER, 1));
        assertEquals(52, weeks.size());

        /*
         * 051015;121215;a
         * 110116;190316;sp
         * 250416;020716;su
         */

        Pair<Integer, LocalDateRange> week1 = weeks.get(0);
        assertEquals(1, week1.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2015, Month.OCTOBER, 5),
                LocalDate.of(2015, Month.OCTOBER, 12)
            ),
            week1.getRight()
        );

        Pair<Integer, LocalDateRange> week52 = weeks.get(51);
        assertEquals(52, week52.getLeft().intValue());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2016, Month.SEPTEMBER, 26),
                LocalDate.of(2016, Month.OCTOBER, 3)
            ),
            week52.getRight()
        );
    }
    
    @Test
    public void getAcademicWeek() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        
        LocalDateRange week1 = LocalDateRange.of(
            LocalDate.of(2006, Month.OCTOBER, 2),
            LocalDate.of(2006, Month.OCTOBER, 9)
        );
        assertEquals(
            week1, factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2), 1)
        );
        assertEquals(
            week1, factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2).plusMonths(5), 1)
        );
        
        try {
            factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2).plusYears(100), 1);
            fail("Should have exceptioned");
        } catch (TermNotFoundException e) {
            // expected
        }
        
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2006, Month.DECEMBER, 4),
                LocalDate.of(2006, Month.DECEMBER, 11)
            ), 
            factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2), 10)
        );
        
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.FEBRUARY, 12),
                LocalDate.of(2007, Month.FEBRUARY, 19)
            ), 
            factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2), 20)
        );
        
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.APRIL, 23),
                LocalDate.of(2007, Month.APRIL, 30)
            ), 
            factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2), 30)
        );
        
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.SEPTEMBER, 24),
                LocalDate.of(2007, Month.OCTOBER, 1)
            ), 
            factory.getAcademicWeek(LocalDate.of(2006, Month.OCTOBER, 2), 52)
        );
    }
    
    @Test
    public void sbtwo3948() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        
        LocalDate monday = LocalDate.of(2011, Month.APRIL, 25);
        
        // This should be week 1 of the Summer term
        Term term = factory.getTermFromDate(monday);

//        This term started on Wednesday 27th April, and finished on Saturday 2nd July
//        We now assume Monday - Sunday, so the term is Monday 25th April - Sunday 3rd July.
        assertEquals(LocalDate.of(2011, Month.APRIL, 25), term.getStartDate());
        assertEquals(LocalDate.of(2011, Month.JULY, 3), term.getEndDate());
        assertEquals(TermType.summer, term.getTermType());
        
        assertEquals(1, term.getWeekNumber(monday));
        assertEquals(30, term.getAcademicWeekNumber(monday));
        assertEquals(21, term.getCumulativeWeekNumber(monday));
        
        assertEquals(1, term.getWeekNumber(monday.with(DayOfWeek.SATURDAY)));
        assertEquals(30, term.getAcademicWeekNumber(monday.with(DayOfWeek.SATURDAY)));
        assertEquals(21, term.getCumulativeWeekNumber(monday.with(DayOfWeek.SATURDAY)));
        
        assertEquals(2, term.getWeekNumber(monday.plusWeeks(1)));
        assertEquals(31, term.getAcademicWeekNumber(monday.plusWeeks(1)));
        assertEquals(22, term.getCumulativeWeekNumber(monday.plusWeeks(1)));
    }
    
    @Test
    public void sanity() throws Exception {
        TermFactoryImpl factory = new TermFactoryImpl();
        List<Term> terms = factory.getTermDates();
        
        Term lastTerm = null;
		for (Term term : terms) {
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

			LocalDate actualEndDate = term.getEndDate();
			while (actualEndDate.getDayOfWeek() != term.getStartDate().getDayOfWeek()) {
				actualEndDate = actualEndDate.plusDays(1);
			}

			assertEquals(10, Weeks.between(term.getStartDate(), actualEndDate).getAmount());

			lastTerm = term;
		}
    }

	@Test
	public void tab2625() throws Exception {
		TermFactoryImpl factory = new TermFactoryImpl();

		LocalDate monday = LocalDate.of(2011, Month.APRIL, 25);
		Term term = factory.getTermFromDate(monday);

		// The end date should be inclusive
		assertEquals(term, factory.getTermFromDate(term.getEndDate()));
	}

}
