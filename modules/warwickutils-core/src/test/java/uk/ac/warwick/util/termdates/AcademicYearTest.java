package uk.ac.warwick.util.termdates;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.junit.Test;
import org.threeten.extra.LocalDateRange;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.*;

public class AcademicYearTest {

    @Test
    public void sanityCheck2011() throws Exception {
        AcademicYear year = AcademicYear.starting(2011);

        AcademicWeek firstWeek = year.getAcademicWeek(LocalDate.of(2011, Month.AUGUST, 1));

        assertEquals(-8, firstWeek.getWeekNumber());
        assertEquals("Starts on a Monday", LocalDate.of(2011, Month.AUGUST, 1), firstWeek.getDateRange().getStart());
        assertEquals(LocalDate.of(2011, Month.AUGUST, 7), firstWeek.getDateRange().getEndInclusive());

        Vacation preTermVacation = (Vacation) year.getPeriod(AcademicYearPeriod.PeriodType.preTermVacation);
        assertEquals(-8, preTermVacation.getFirstWeek().getWeekNumber());
        assertEquals("Starts on a Monday", LocalDate.of(2011, Month.AUGUST, 1), preTermVacation.getFirstWeek().getDateRange().getStart());

        AcademicWeek week0 = preTermVacation.getLastWeek();
        assertEquals(0, week0.getWeekNumber());
    }

    @Test
    public void sanityCheck2017() throws Exception {
        AcademicYear year = AcademicYear.starting(2017);

        AcademicWeek firstWeek = year.getAcademicWeek(LocalDate.of(2017, Month.AUGUST, 1));

        assertEquals(-8, firstWeek.getWeekNumber());
        assertEquals("Starts on a Tuesday", LocalDate.of(2017, Month.AUGUST, 1), firstWeek.getDateRange().getStart());
        assertEquals(LocalDate.of(2017, Month.AUGUST, 6), firstWeek.getDateRange().getEndInclusive());

        Vacation preTermVacation = (Vacation) year.getPeriod(AcademicYearPeriod.PeriodType.preTermVacation);
        assertEquals(-8, preTermVacation.getFirstWeek().getWeekNumber());
        assertEquals("Starts on a Tuesday", LocalDate.of(2017, Month.AUGUST, 1), preTermVacation.getFirstWeek().getDateRange().getStart());

        AcademicWeek week0 = preTermVacation.getLastWeek();
        assertEquals(0, week0.getWeekNumber());
    }

    @Test
    public void sanityCheckData() throws Exception {
        for (int startYear = 2006; startYear <= 2025; startYear++) {
            AcademicYear year = AcademicYear.starting(startYear);
            assertEquals(year, AcademicYear.forDate(LocalDate.of(startYear + 1, Month.APRIL, 23)));
            assertEquals(year, AcademicYear.parse(Integer.toString(startYear).substring(2) + "/99")); // end year is ignored anyway

            assertEquals(startYear, year.getStartYear());
            assertEquals(startYear, year.getValue());
            assertTrue(year.getLabel().startsWith(Integer.toString(startYear).substring(2) + "/"));

            assertEquals("Should have all 7 periods", 7, year.getPeriods().size());
            assertTrue("Always start with a vacation", year.getPeriods().iterator().next().isVacation());

            // Periods should go up consecutively
            AcademicYearPeriod lastPeriod = null;
            for (AcademicYearPeriod period : year.getPeriods()) {
                switch (period.getType()) {
                    case preTermVacation:
                        assertTrue("Pre-term vacation always starts in week -8 or -9", period.getFirstWeek().getWeekNumber() <= -8);
                        assertEquals("Pre-term vacation always ends with week 0", 0, period.getLastWeek().getWeekNumber());
                        break;
                    case autumnTerm:
                        assertEquals("Autumn term always starts with week 1", 1, period.getFirstWeek().getWeekNumber());
                        assertEquals("Autumn term always ends with week 10", 10, period.getLastWeek().getWeekNumber());
                        break;
                    case christmasVacation:
                        assertEquals("Christmas vacation always starts with week 11", 11, period.getFirstWeek().getWeekNumber());
                        break;
                }

                assertEquals(year, period.getYear());
                assertEquals(period, period.getFirstWeek().getPeriod());
                assertEquals(year, period.getFirstWeek().getYear());
                assertEquals(period, period.getLastWeek().getPeriod());
                assertEquals(year, period.getLastWeek().getYear());

                if (lastPeriod != null) {
                    assertTrue(lastPeriod.compareTo(period) < 0);
                    assertTrue(period.compareTo(lastPeriod) > 0);
                    assertEquals("Week should follow on from previous", lastPeriod.getLastWeek().getWeekNumber() + 1, period.getFirstWeek().getWeekNumber());
                }

                lastPeriod = period;
            }

            assertTrue("Should have at least 52 weeks", year.getAcademicWeeks().size() >= 52);
            assertTrue("Should have at most 53 weeks", year.getAcademicWeeks().size() <= 53);
            assertTrue("First week number should be negative", year.getAcademicWeeks().iterator().next().getWeekNumber() < 0);
            assertEquals("First week should always start on 01/08", LocalDate.of(startYear, Month.AUGUST, 1), year.getAcademicWeeks().iterator().next().getDateRange().getStart());
            assertEquals("Last week should always end on 31/07", LocalDate.of(startYear + 1, Month.JULY, 31), year.getAcademicWeeks().get(year.getAcademicWeeks().size() - 1).getDateRange().getEndInclusive());

            // Week numbers should go up consecutively
            AcademicWeek lastWeek = null;
            for (AcademicWeek week : year.getAcademicWeeks()) {
                if (lastWeek != null) {
                    assertTrue(lastWeek.compareTo(week) < 0);
                    assertTrue(week.compareTo(lastWeek) > 0);
                    assertEquals("Week should follow on from previous", lastWeek.getWeekNumber() + 1, week.getWeekNumber());
                }

                if (week.getPeriod().isTerm()) {
                    assertTrue(week.getTermWeekNumber() > 0);
                    assertTrue(week.getCumulativeWeekNumber() > 0);
                }

                lastWeek = week;
            }
        }
    }

    @Test
    public void legacyCompatible() throws Exception {
        AcademicYear year = AcademicYear.starting(2006);

        AcademicYearPeriod period = year.getPeriod(LocalDate.of(2007, Month.APRIL, 23));
        assertTrue(period.isTerm());
        assertEquals(AcademicYearPeriod.PeriodType.summerTerm, period.getType());

        AcademicWeek week = year.getAcademicWeek(LocalDate.of(2007, Month.APRIL, 23));
        assertEquals(period, week.getPeriod());
        assertEquals(year, week.getYear());
        assertEquals(30, week.getWeekNumber());
        assertEquals(1, week.getTermWeekNumber());
        assertEquals(21, week.getCumulativeWeekNumber());
        assertEquals(LocalDateRange.of(LocalDate.of(2007, Month.APRIL, 23), LocalDate.of(2007, Month.APRIL, 30)), week.getDateRange());
    }

    @Test
    public void enoughDates() throws Exception {
        // Check that we have 3 years data going forward
        AcademicYear now = AcademicYear.forDate(LocalDate.now());
        for (AcademicYear year : now.yearsSurrounding(0, 3)) {
            assertFalse(year.getPeriods().isEmpty()); // This will throw an exception if it ends up with a placeholder year
        }
    }

    @Test
    public void getAcademicWeeksForYear() throws Exception {
        AcademicYear year = AcademicYear.forDate(LocalDate.of(2007, Month.APRIL, 23));
        assertEquals(53, year.getAcademicWeeks().size());
        
        /*
         * 021006;091206;a
         * 080107;170307;sp
         * 230407;300607;su
         */

        AcademicWeek week1 = year.getAcademicWeek(1);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2006, Month.OCTOBER, 2),
                LocalDate.of(2006, Month.OCTOBER, 9)
            ),
            week1.getDateRange()
        );

        AcademicWeek week10 = year.getAcademicWeek(10);
        assertEquals(10, week10.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2006, Month.DECEMBER, 4),
                LocalDate.of(2006, Month.DECEMBER, 11)
            ),
            week10.getDateRange()
        );

        AcademicWeek week20 = year.getAcademicWeek(20);
        assertEquals(20, week20.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.FEBRUARY, 12),
                LocalDate.of(2007, Month.FEBRUARY, 19)
            ),
            week20.getDateRange()
        );

        AcademicWeek week30 = year.getAcademicWeek(30);
        assertEquals(30, week30.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2007, Month.APRIL, 23),
                LocalDate.of(2007, Month.APRIL, 30)
            ),
            week30.getDateRange()
        );
    }

    @Test
    public void getAcademicWeeksForYear2014() throws Exception {
        // 2014/15 is special, it has 53 weeks

        AcademicYear year = AcademicYear.forDate(LocalDate.of(2014, Month.NOVEMBER, 1));
        assertEquals(53, year.getAcademicWeeks().size());

        /*
         * 290914;061214;a
         * 050115;140315;sp
         * 200415;270615;su
         */

        AcademicWeek week1 = year.getAcademicWeek(1);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2014, Month.SEPTEMBER, 29),
                LocalDate.of(2014, Month.OCTOBER, 6)
            ),
            week1.getDateRange()
        );

        AcademicWeek week42 = year.getAcademicWeek(42);
        assertEquals(42, week42.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2015, Month.JULY, 13),
                LocalDate.of(2015, Month.JULY, 20)
            ),
            week42.getDateRange()
        );
    }

    @Test
    public void getAcademicWeeksForYear2015() throws Exception {
        AcademicYear year = AcademicYear.forDate(LocalDate.of(2015, Month.NOVEMBER, 1));
        assertEquals(53, year.getAcademicWeeks().size());

        /*
         * 051015;121215;a
         * 110116;190316;sp
         * 250416;020716;su
         */

        AcademicWeek week1 = year.getAcademicWeek(1);
        assertEquals(1, week1.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2015, Month.OCTOBER, 5),
                LocalDate.of(2015, Month.OCTOBER, 12)
            ),
            week1.getDateRange()
        );

        AcademicWeek week42 = year.getAcademicWeek(42);
        assertEquals(42, week42.getWeekNumber());
        assertEquals(
            LocalDateRange.of(
                LocalDate.of(2016, Month.JULY, 18),
                LocalDate.of(2016, Month.JULY, 25)
            ),
            week42.getDateRange()
        );
    }

    @Test
    public void sbtwo3948() throws Exception {
        LocalDate monday = LocalDate.of(2011, Month.APRIL, 25);

        // This should be week 1 of the Summer term
        AcademicYear year = AcademicYear.forDate(monday);
        AcademicYearPeriod term = year.getPeriod(monday);

//        This term started on Wednesday 27th April, and finished on Saturday 2nd July
//        We now assume Monday - Sunday, so the term is Monday 25th April - Sunday 3rd July.
        assertEquals(LocalDate.of(2011, Month.APRIL, 25), term.getFirstDay());
        assertEquals(LocalDate.of(2011, Month.JULY, 3), term.getLastDay());
        assertEquals(AcademicYearPeriod.PeriodType.summerTerm, term.getType());

        assertEquals(1, year.getAcademicWeek(monday).getTermWeekNumber());
        assertEquals(30, year.getAcademicWeek(monday).getWeekNumber());
        assertEquals(21, year.getAcademicWeek(monday).getCumulativeWeekNumber());

        assertEquals(1, year.getAcademicWeek(monday.with(DayOfWeek.SATURDAY)).getTermWeekNumber());
        assertEquals(30, year.getAcademicWeek(monday.with(DayOfWeek.SATURDAY)).getWeekNumber());
        assertEquals(21, year.getAcademicWeek(monday.with(DayOfWeek.SATURDAY)).getCumulativeWeekNumber());

        assertEquals(2, year.getAcademicWeek(monday.plusWeeks(1)).getTermWeekNumber());
        assertEquals(31, year.getAcademicWeek(monday.plusWeeks(1)).getWeekNumber());
        assertEquals(22, year.getAcademicWeek(monday.plusWeeks(1)).getCumulativeWeekNumber());
    }

    @Test
    public void tab2625() throws Exception {
        LocalDate monday = LocalDate.of(2011, Month.APRIL, 25);
        AcademicYear year = AcademicYear.forDate(monday);
        AcademicYearPeriod term = year.getPeriod(monday);

        // The end date should be inclusive
        assertEquals(term, year.getPeriod(term.getLastDay()));
    }

    @Test
    public void serializable() throws Exception {
        SerializingTranscoder transcoder = new SerializingTranscoder();
        AcademicYear year = AcademicYear.starting(2017);
        CachedData encoded = transcoder.encode(year);
        assertNotNull(encoded);
        assertEquals(year, transcoder.decode(encoded));
    }

}