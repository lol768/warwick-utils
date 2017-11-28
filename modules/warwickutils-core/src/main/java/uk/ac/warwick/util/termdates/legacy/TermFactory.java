package uk.ac.warwick.util.termdates.legacy;

import org.threeten.extra.LocalDateRange;
import uk.ac.warwick.util.collections.Pair;

import java.time.temporal.Temporal;
import java.util.List;

/**
 * @deprecated Use {@link uk.ac.warwick.util.termdates.AcademicYear}
 */
public interface TermFactory {
    Term getTermFromDate(Temporal date) throws TermNotFoundException;
    Term getPreviousTerm(Term term) throws TermNotFoundException;
    Term getNextTerm(Term term) throws TermNotFoundException;

    LocalDateRange getAcademicWeek(Temporal date, int weekNumber) throws TermNotFoundException;
    List<Pair<Integer, LocalDateRange>> getAcademicWeeksForYear(Temporal date) throws TermNotFoundException;
}
