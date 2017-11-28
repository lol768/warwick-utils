package uk.ac.warwick.util.termdates;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.threeten.extra.LocalDateRange;

import java.time.LocalDate;

public class AcademicWeek implements Comparable<AcademicWeek> {

    private final AcademicYear academicYear;

    private final AcademicYearPeriod period;

    private final int weekNumber;

    private final LocalDateRange dateRange;

    private AcademicWeek(AcademicYear academicYear, AcademicYearPeriod period, int weekNumber, LocalDateRange dateRange) {
        this.academicYear = academicYear;
        this.period = period;
        this.weekNumber = weekNumber;
        this.dateRange = dateRange;
    }

    static AcademicWeek of(AcademicYear academicYear, AcademicYearPeriod period, int weekNumber, LocalDateRange dateRange) {
        return new AcademicWeek(academicYear, period, weekNumber, dateRange);
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public AcademicYearPeriod getPeriod() {
        return period;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public int getTermWeekNumber() {
        if (!period.isTerm()) throw new IllegalStateException();

        int number = 1;
        LocalDate startDate = period.getFirstDay();
        while (!dateRange.contains(startDate)) {
            number++;
            startDate = startDate.plusWeeks(1);
        }

        return number;
    }

    public int getCumulativeWeekNumber() {
        if (!period.isTerm()) throw new IllegalStateException();

        final int modifier;
        switch (period.getType()) {
            case springTerm:
                modifier = 10;
                break;
            case summerTerm:
                modifier = 20;
                break;
            default:
                modifier = 0;
        }

        return modifier + getTermWeekNumber();
    }

    public LocalDateRange getDateRange() {
        return dateRange;
    }

    @Override
    public int compareTo(AcademicWeek o) {
        int result = this.academicYear.compareTo(o.academicYear);
        if (result != 0) return result;

        result = this.period.compareTo(o.period);
        if (result != 0) return result;

        return this.weekNumber - o.weekNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AcademicWeek that = (AcademicWeek) o;

        return new EqualsBuilder()
            .append(weekNumber, that.weekNumber)
            .append(academicYear, that.academicYear)
            .append(period, that.period)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(academicYear)
            .append(period)
            .append(weekNumber)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("academicYear", academicYear)
            .append("period", period)
            .append("weekNumber", weekNumber)
            .append("dateRange", dateRange)
            .toString();
    }
}
