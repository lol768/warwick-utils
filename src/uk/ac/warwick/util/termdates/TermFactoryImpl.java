package uk.ac.warwick.util.termdates;

import java.util.Collections;
import java.util.LinkedList;

import org.joda.time.base.BaseDateTime;

public final class TermFactoryImpl implements TermFactory {
    
    private LinkedList<Term> termDates;

    public Term getTermFromDate(final BaseDateTime date) throws TermNotFoundException {
        for (Term term : termDates) {
            if (date.isBefore(term.getEndDate())) {
                return term;
            }
        }
        throw new TermNotFoundException("Could not find a term date for date: " + date.toString());
    }
    
    @SuppressWarnings("unchecked")
    public Term getPreviousTerm(final Term term) throws TermNotFoundException {
        LinkedList<Term> reversedTermDates = (LinkedList<Term>)termDates.clone();
        Collections.reverse(reversedTermDates);
        
        for (Term thisTerm : reversedTermDates) {
            if (thisTerm.getStartDate().isBefore(term.getStartDate())) {
                return thisTerm;
            }
        }
        throw new TermNotFoundException("Could not find a term before date: " + term.getStartDate().toString());
    }
    
    public Term getNextTerm(final Term term) throws TermNotFoundException {
        return getTermFromDate(term.getEndDate().plusDays(1));
    }
    
    public void setTermDates(final LinkedList<Term> termDates) {
        this.termDates = termDates;
    }

}
