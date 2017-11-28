package uk.ac.warwick.util.termdates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;

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

    private final PeriodType type;

    private final LocalDate firstDay;

    private final LocalDate lastDay;

    AcademicYearPeriod(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        this.type = type;
        this.firstDay = firstDay;
        this.lastDay = lastDay;
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
            .append(type, that.type)
            .append(firstDay, that.firstDay)
            .append(lastDay, that.lastDay)
            .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(type)
            .append(firstDay)
            .append(lastDay)
            .toHashCode();
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("type", type)
            .append("firstDay", firstDay)
            .append("lastDay", lastDay)
            .toString();
    }
}
