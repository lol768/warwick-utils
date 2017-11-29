package uk.ac.warwick.util.termdates;

import java.time.LocalDate;

public class Term extends AcademicYearPeriod {

    private Term(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        super(type, firstDay, lastDay);
        if (!type.isValid(Term.class)) {
            throw new IllegalArgumentException(type + " isn't valid for Terms");
        }
    }

    static Term of(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        return new Term(type, firstDay, lastDay);
    }
}
