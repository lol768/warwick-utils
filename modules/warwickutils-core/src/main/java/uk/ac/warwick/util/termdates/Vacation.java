package uk.ac.warwick.util.termdates;

import java.time.LocalDate;

public class Vacation extends AcademicYearPeriod {

    private Vacation(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        super(type, firstDay, lastDay);
        if (!type.isValid(Vacation.class)) {
            throw new IllegalArgumentException(type + " isn't valid for Vacations");
        }
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
