package uk.ac.warwick.util.termdates;

import java.time.LocalDate;

public class Vacation extends AcademicYearPeriod {

    private Vacation(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        this(null, type, firstDay, lastDay);
    }

    private Vacation(AcademicYear year, PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        super(year, type, firstDay, lastDay);
        if (!type.isValid(Vacation.class)) {
            throw new IllegalArgumentException(type + " isn't valid for Vacations");
        }
    }

    @Override
    Vacation withYear(AcademicYear year) {
        return new Vacation(year, getType(), getFirstDay(), getLastDay());
    }

    static Vacation between(PeriodType type, Term before, Term after) {
        return new Vacation(type, before.getLastDay().plusDays(1), after.getFirstDay().minusDays(1));
    }

    static Vacation between(PeriodType type, LocalDate firstDay, Term after) {
        return new Vacation(type, firstDay, after.getFirstDay().minusDays(1));
    }

    static Vacation between(PeriodType type, Term before, LocalDate lastDay) {
        return new Vacation(type, before.getLastDay().plusDays(1), lastDay);
    }
}
