package uk.ac.warwick.util.termdates;

import java.util.List;

import org.joda.time.Interval;
import org.joda.time.base.BaseDateTime;

import uk.ac.warwick.util.collections.Pair;

public interface TermFactory {
    
    Term getTermFromDate(BaseDateTime date) throws TermNotFoundException;
    Term getPreviousTerm(Term term) throws TermNotFoundException;
    Term getNextTerm(Term term) throws TermNotFoundException;
    
    Interval getAcademicWeek(BaseDateTime date, int weekNumber) throws TermNotFoundException;
    List<Pair<Integer, Interval>> getAcademicWeeksForYear(BaseDateTime date) throws TermNotFoundException;
}
