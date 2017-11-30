package uk.ac.warwick.util.termdates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.threeten.extra.LocalDateRange;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Ordering.*;
import static java.lang.Integer.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

public class AcademicYear implements Comparable<AcademicYear>, Serializable {

    private static final Pattern SITS_PATTERN = Pattern.compile("(\\d{2})/(\\d{2})");

    /**
     * We're only dealing with current years, not DOBs or anything, so can afford
     * to make the century break large. I don't think there is even any module data in SITS
     * from before 2004, so could even do without this check.
     *
     * Anyway, this will only break near the year 2090.
     */
    private static final int CENTURY_BREAK = 90;

    private final int startYear;

    private final Map<AcademicYearPeriod.PeriodType, AcademicYearPeriod> periods;

    private final Map<Integer, AcademicWeek> weeks;

    private AcademicYear(int startYear, List<AcademicYearPeriod> periods) {
        // Ensure yy/yy formatting is valid
        verify(startYear >= 1000 && startYear < 9999, "Invalid start year: " + startYear);

        this.startYear = startYear;
        this.periods = periods.stream().map(period -> period.withYear(this)).collect(toMap(AcademicYearPeriod::getType, identity()));
        this.weeks = buildWeeks();
    }

    private Map<Integer, AcademicWeek> buildWeeks() {
        if (periods.isEmpty()) return emptyMap();

        ImmutableMap.Builder<Integer, AcademicWeek> weeks = ImmutableMap.builder();
        LocalDate firstDayOfAutumnTerm = periods.get(AcademicYearPeriod.PeriodType.autumnTerm).getFirstDay();
        LocalDate firstDayOfAcademicYear = firstDayOfAutumnTerm.with(Month.AUGUST).withDayOfMonth(1);

        int offset = (int) ChronoUnit.WEEKS.between(firstDayOfAcademicYear, firstDayOfAutumnTerm);

        // Off-by-one error if the first day is a Monday (so there is an extra full week being counted in error)
        if (firstDayOfAcademicYear.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            offset -= 1;
        }

        int weekNumber = -offset;
        for (AcademicYearPeriod period : getSortedValues(periods)) {
            LocalDate start = period.getFirstDay();
            LocalDate end = start.plusWeeks(1);
            if (!start.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                end = end.with(DayOfWeek.MONDAY);
            }

            while (end.isBefore(period.getLastDay())) {
                weeks.put(weekNumber, AcademicWeek.of(this, period, weekNumber, LocalDateRange.of(start, end)));
                start = end;
                end = start.plusWeeks(1);
                weekNumber++;
            }

            // The last period
            end = period.getLastDay().plusDays(1);
            weeks.put(weekNumber, AcademicWeek.of(this, period, weekNumber, LocalDateRange.of(start, end)));
            weekNumber++;
        }

        return weeks.build();
    }

    static AcademicYear build(AcademicYearPeriod... periods) {
        verify(periods.length > 0, "Must provide at least one AcademicYearPeriod");

        List<AcademicYearPeriod> sortedPeriods = unmodifiableList(natural().sortedCopy(asList(periods)));

        // Ensure we have a Term and Vacation of every type
        verify(sortedPeriods.size() == AcademicYearPeriod.PeriodType.values().length, "Must provide an AcademicYearPeriod of every type");

        for (AcademicYearPeriod.PeriodType periodType : AcademicYearPeriod.PeriodType.values()) {
            verify(sortedPeriods.stream().anyMatch(p -> p.getType() == periodType));
        }

        return new AcademicYear(sortedPeriods.iterator().next().getFirstDay().getYear(), sortedPeriods);
    }

    static AcademicYear placeholder(int startYear) {
        return new AcademicYear(startYear, emptyList());
    }

    public static AcademicYear starting(int startYear) {
        return TermDatesService.INSTANCE.getAcademicYear(startYear);
    }

    public static AcademicYear forDate(Temporal temporal) {
        YearMonth yearMonth = YearMonth.from(temporal);
        return (yearMonth.getMonthValue() < Month.AUGUST.getValue()) ? starting(yearMonth.getYear() - 1) : starting(yearMonth.getYear());
    }

    public static AcademicYear parse(String pattern) {
        Matcher m = SITS_PATTERN.matcher(pattern);

        if (m.matches()) {
            int startYear = parseInt(m.group(1));
            return (startYear > CENTURY_BREAK) ? starting(1900 + startYear) : starting(2000 + startYear);
        } else {
            throw new IllegalArgumentException("Did not match YY/YY: " + pattern);
        }
    }

    public int getStartYear() {
        return startYear;
    }

    private static <K, V extends Comparable<? super V>> List<V> getSortedValues(Map<K, V> map) {
        return map.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getValue).collect(toList());
    }

    public List<AcademicYearPeriod> getPeriods() {
        if (periods.isEmpty()) {
            throw new IllegalStateException("No term dates found for " + toString());
        }

        return getSortedValues(periods);
    }

    public AcademicYearPeriod getPeriod(AcademicYearPeriod.PeriodType type) {
        if (periods.isEmpty()) {
            throw new IllegalStateException("No term dates found for " + toString());
        }

        return periods.get(type);
    }

    public AcademicYearPeriod getPeriod(Temporal date) {
        return getAcademicWeek(date).getPeriod();
    }

    public List<AcademicWeek> getAcademicWeeks() {
        if (periods.isEmpty()) {
            throw new IllegalStateException("No term dates found for " + toString());
        }

        return getSortedValues(weeks);
    }

    public AcademicWeek getAcademicWeek(int weekNumber) {
        if (periods.isEmpty()) {
            throw new IllegalStateException("No term dates found for " + toString());
        } else if (!weeks.containsKey(weekNumber)) {
            throw new IllegalArgumentException("Invalid week number: " + weekNumber + " for " + toString());
        }

        return weeks.get(weekNumber);
    }

    public AcademicWeek getAcademicWeek(Temporal date) {
        if (periods.isEmpty()) {
            throw new IllegalStateException("No term dates found for " + toString());
        }

        return getAcademicWeeks().stream()
            .filter(w -> w.getDateRange().contains(LocalDate.from(date)))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No week found for " + date));
    }

    public int getValue() {
        return getStartYear();
    }

    public String getLabel() {
        return toString();
    }

    public AcademicYear previous() {
        return starting(startYear - 1);
    }

    public AcademicYear next() {
        return starting(startYear + 1);
    }

    public List<AcademicYear> yearsSurrounding(int yearsBefore, int yearsAfter) {
        verify(yearsBefore >= 0 && yearsAfter >= 0);
        ImmutableList.Builder<AcademicYear> years = ImmutableList.builder();

        for (int year = startYear - yearsBefore; year <= startYear + yearsAfter; year++) {
            years.add(starting(year));
        }

        return years.build();
    }

    public boolean isBefore(AcademicYear other) {
        return this.compareTo(other) < 0;
    }

    public boolean isAfter(AcademicYear other) {
        return this.compareTo(other) > 0;
    }

    @Override
    public int compareTo(AcademicYear o) {
        return this.startYear - o.startYear;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcademicYear that = (AcademicYear) o;
        return this.startYear == that.startYear;
    }

    @Override
    public String toString() {
        return String.format("%s/%s", Integer.toString(startYear).substring(2), Integer.toString(startYear + 1).substring(2));
    }

    private static void verify(boolean condition) {
        if (!condition) throw new IllegalArgumentException();
    }

    private static void verify(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

}
