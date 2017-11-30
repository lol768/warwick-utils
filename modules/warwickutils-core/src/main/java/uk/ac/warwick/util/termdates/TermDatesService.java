package uk.ac.warwick.util.termdates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.warwick.util.core.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static java.util.stream.Collectors.*;

class TermDatesService {

    static final TermDatesService INSTANCE;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");

    private final Map<Integer, AcademicYear> academicYears;

    private final Map<Integer, ExtendedAcademicYear> extendedAcademicYears;

    private TermDatesService() {
        try {
            ImmutableList.Builder<Term> termsBuilder = ImmutableList.builder();

            String source = StringUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("/uk/ac/warwick/util/termdates/termdates.txt")));
            for (StringTokenizer st = new StringTokenizer(source, "\n"); st.hasMoreTokens();) {
                String line = st.nextToken().trim();
                String[] data = line.split(";");
                String firstDayString = data[0];
                String lastDayString = data[1];
                String termTypeString = data[2];

                LocalDate firstDay = LocalDate.parse(firstDayString, DATE_FORMATTER).with(DayOfWeek.MONDAY);
                LocalDate lastDay = LocalDate.parse(lastDayString, DATE_FORMATTER).with(DayOfWeek.SUNDAY);

                final AcademicYearPeriod.PeriodType type;
                switch (termTypeString) {
                    case "a":
                        type = AcademicYearPeriod.PeriodType.autumnTerm;
                        break;
                    case "sp":
                        type = AcademicYearPeriod.PeriodType.springTerm;
                        break;
                    case "su":
                        type = AcademicYearPeriod.PeriodType.summerTerm;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid term string");
                }

                termsBuilder.add(Term.of(type, firstDay, lastDay));
            }

            List<Term> terms = termsBuilder.build();
            validate(terms);

            ImmutableMap.Builder<Integer, AcademicYear> academicYears = ImmutableMap.builder();
            Iterator<Term> itr = terms.iterator();
            while (itr.hasNext()) {
                Term autumnTerm = itr.next();
                Term springTerm = itr.next();
                Term summerTerm = itr.next();

                Vacation preTermVacation = Vacation.between(AcademicYearPeriod.PeriodType.preTermVacation, autumnTerm.getFirstDay().with(Month.AUGUST).withDayOfMonth(1), autumnTerm);
                Vacation winterVacation = Vacation.between(AcademicYearPeriod.PeriodType.christmasVacation, autumnTerm, springTerm);
                Vacation easterVacation = Vacation.between(AcademicYearPeriod.PeriodType.easterVacation, springTerm, summerTerm);
                Vacation summerVacation = Vacation.between(AcademicYearPeriod.PeriodType.summerVacation, summerTerm, summerTerm.getLastDay().with(Month.JULY).withDayOfMonth(31));

                AcademicYear year = AcademicYear.build(preTermVacation, autumnTerm, winterVacation, springTerm, easterVacation, summerTerm, summerVacation);
                academicYears.put(year.getStartYear(), year);
            }

            this.academicYears = academicYears.build();

            ImmutableMap.Builder<Integer, ExtendedAcademicYear> extendedAcademicYears = ImmutableMap.builder();
            AcademicYear previousYear = null;
            for (AcademicYear year : this.academicYears.values().stream().sorted().collect(toList())) {
                if (previousYear != null) {
                    extendedAcademicYears.put(previousYear.getStartYear(), ExtendedAcademicYear.wrap(previousYear, year));
                }

                previousYear = year;
            }
            this.extendedAcademicYears = extendedAcademicYears.build();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't instantiate a TermDatesService", e);
        }
    }

    AcademicYear getAcademicYear(int startYear) {
        return academicYears.getOrDefault(startYear, AcademicYear.placeholder(startYear));
    }

    ExtendedAcademicYear getExtendedAcademicYear(int startYear) {
        return extendedAcademicYears.getOrDefault(startYear, ExtendedAcademicYear.placeholder(startYear));
    }

    private static void validate(List<Term> terms) {
        // Check that the terms go in order
        verify(!terms.isEmpty(), "Terms must be found");

        Iterator<Term> itr = terms.iterator();
        Term lastTerm = itr.next();
        verify(lastTerm.getType() == AcademicYearPeriod.PeriodType.autumnTerm, "Must start with an Autumn term");

        while (itr.hasNext()) {
            Term nextTerm = itr.next();
            switch (lastTerm.getType()) {
                case autumnTerm:
                    verify(nextTerm.getType() == AcademicYearPeriod.PeriodType.springTerm, "Spring must follow Autumn");
                    break;
                case springTerm:
                    verify(nextTerm.getType() == AcademicYearPeriod.PeriodType.summerTerm, "Summer must follow Spring");
                    break;
                case summerTerm:
                    verify(nextTerm.getType() == AcademicYearPeriod.PeriodType.autumnTerm, "Autumn must follow Summer");
                    break;
            }

            verify(nextTerm.isAfter(lastTerm), "Must follow previous term");

            lastTerm = nextTerm;
        }

        verify(lastTerm.getType() == AcademicYearPeriod.PeriodType.summerTerm, "Must end with a Summer term");
    }

    private static void verify(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    static {
        INSTANCE = new TermDatesService();
    }

}
