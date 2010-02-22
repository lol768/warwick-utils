package uk.ac.warwick.util.termdates;

import org.joda.time.base.BaseDateTime;

public interface TermFactory {
    
    Term getTermFromDate(BaseDateTime date) throws TermNotFoundException;
    Term getPreviousTerm(Term term) throws TermNotFoundException;
    Term getNextTerm(Term term) throws TermNotFoundException;
}
