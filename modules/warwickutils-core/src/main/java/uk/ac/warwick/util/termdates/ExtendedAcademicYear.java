package uk.ac.warwick.util.termdates;

import com.google.common.collect.ImmutableList;

import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.regex.Matcher;

import static java.lang.Integer.*;
import static java.util.Collections.*;

public class ExtendedAcademicYear extends AcademicYear {

    private ExtendedAcademicYear(int startYear, List<AcademicYearPeriod> periods) {
        super(startYear, periods);
    }

    static ExtendedAcademicYear wrap(AcademicYear year, AcademicYear next) {
        if (year.isPlaceholder() || next.isPlaceholder()) {
            return placeholder(year.getStartYear());
        }

        // Extend the summer vacation
        ImmutableList.Builder<AcademicYearPeriod> periods = ImmutableList.builder();
        for (AcademicYearPeriod period : year.getPeriods()) {
            if (period.getType() == AcademicYearPeriod.PeriodType.summerVacation) {
                periods.add(
                    Vacation.between(
                        AcademicYearPeriod.PeriodType.summerVacation,
                        (Term) year.getPeriod(AcademicYearPeriod.PeriodType.summerTerm),
                        (Term) next.getPeriod(AcademicYearPeriod.PeriodType.autumnTerm)
                    )
                );
            } else {
                periods.add(period);
            }
        }

        return new ExtendedAcademicYear(year.getStartYear(), periods.build());
    }

    static ExtendedAcademicYear placeholder(int startYear) {
        return new ExtendedAcademicYear(startYear, emptyList());
    }

    public static ExtendedAcademicYear starting(int startYear) {
        return TermDatesService.INSTANCE.getExtendedAcademicYear(startYear);
    }

    public static ExtendedAcademicYear forDate(Temporal temporal) {
        YearMonth yearMonth = YearMonth.from(temporal);
        return (yearMonth.getMonthValue() < Month.AUGUST.getValue()) ? starting(yearMonth.getYear() - 1) : starting(yearMonth.getYear());
    }

    public static ExtendedAcademicYear parse(String pattern) {
        Matcher m = SITS_PATTERN.matcher(pattern);

        if (m.matches()) {
            int startYear = parseInt(m.group(1));
            return (startYear > CENTURY_BREAK) ? starting(1900 + startYear) : starting(2000 + startYear);
        } else {
            throw new IllegalArgumentException("Did not match YY/YY: " + pattern);
        }
    }

    @Override
    public ExtendedAcademicYear previous() {
        return starting(getStartYear() - 1);
    }

    @Override
    public ExtendedAcademicYear next() {
        return starting(getStartYear() + 1);
    }

    @Override
    public List<AcademicYear> yearsSurrounding(int yearsBefore, int yearsAfter) {
        verify(yearsBefore >= 0 && yearsAfter >= 0);
        ImmutableList.Builder<AcademicYear> years = ImmutableList.builder();

        for (int year = getStartYear() - yearsBefore; year <= getStartYear() + yearsAfter; year++) {
            years.add(starting(year));
        }

        return years.build();
    }
}
