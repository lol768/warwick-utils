package uk.ac.warwick.util.termdates;

import java.time.LocalDate;

public class Term extends AcademicYearPeriod {

    private Term(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        this(null, type, firstDay, lastDay);
    }

    private Term(AcademicYear year, PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        super(year, type, firstDay, lastDay);
        if (!type.isValid(Term.class)) {
            throw new IllegalArgumentException(type + " isn't valid for Terms");
        }
    }

    @Override
    Term withYear(AcademicYear year) {
        return new Term(year, getType(), getFirstDay(), getLastDay());
    }

    static Term of(PeriodType type, LocalDate firstDay, LocalDate lastDay) {
        return new Term(type, firstDay, lastDay);
    }
}
