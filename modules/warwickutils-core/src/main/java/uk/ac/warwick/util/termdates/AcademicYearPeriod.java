package uk.ac.warwick.util.termdates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.*;

public abstract class AcademicYearPeriod implements Comparable<AcademicYearPeriod> {

    public enum PeriodType {
        preTermVacation("Pre-term vacation", Vacation.class),
        autumnTerm("Autumn", Term.class),
        christmasVacation("Christmas vacation", Vacation.class),
        springTerm("Spring", Term.class),
        easterVacation("Easter vacation", Vacation.class),
        summerTerm("Summer", Term.class),
        summerVacation("Summer vacation", Vacation.class);

        private final String title;

        private final Class<? extends AcademicYearPeriod> validClass;

        PeriodType(String title, Class<? extends AcademicYearPeriod> validClass) {
            this.title = title;
            this.validClass = validClass;
        }

        boolean isValid(Class<? extends AcademicYearPeriod> clazz) {
            return this.validClass == clazz;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final AcademicYear year;

    private final PeriodType type;

    private final LocalDate firstDay;

    private final LocalDate lastDay;

    AcademicYearPeriod(AcademicYear year, PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        this.year = year;
        this.type = type;
        this.firstDay = firstDay;
        this.lastDay = lastDay;
    }

    abstract AcademicYearPeriod withYear(AcademicYear year);

    public final AcademicYear getYear() {
        return year;
    }

    public final PeriodType getType() {
        return type;
    }

    public final LocalDate getFirstDay() {
        return firstDay;
    }

    public final LocalDate getLastDay() {
        return lastDay;
    }

    public final List<AcademicWeek> getAcademicWeeks() {
        return year.getAcademicWeeks()
            .stream()
            .filter(week -> week.getWeekNumber() >= getFirstWeek().getWeekNumber() && week.getWeekNumber() <= getLastWeek().getWeekNumber())
            .collect(toList());
    }

    public final AcademicWeek getFirstWeek() {
        return year.getAcademicWeek(firstDay);
    }

    public final AcademicWeek getLastWeek() {
        return year.getAcademicWeek(lastDay);
    }

    public final boolean isTerm() {
        return getType().isValid(Term.class);
    }

    public final boolean isVacation() {
        return getType().isValid(Vacation.class);
    }

    public final boolean isBefore(AcademicYearPeriod other) {
        return this.compareTo(other) < 0;
    }

    public final boolean isAfter(AcademicYearPeriod other) {
        return this.compareTo(other) > 0;
    }

    @Override
    public final int compareTo(AcademicYearPeriod o) {
        return firstDay.compareTo(o.firstDay);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AcademicYearPeriod that = (AcademicYearPeriod) o;

        return new EqualsBuilder()
            .append(year, that.year)
            .append(type, that.type)
            .append(firstDay, that.firstDay)
            .append(lastDay, that.lastDay)
            .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(year)
            .append(type)
            .append(firstDay)
            .append(lastDay)
            .toHashCode();
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("year", year)
            .append("type", type)
            .append("firstDay", firstDay)
            .append("lastDay", lastDay)
            .toString();
    }
}
