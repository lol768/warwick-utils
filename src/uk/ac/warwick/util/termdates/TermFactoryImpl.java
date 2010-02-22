package uk.ac.warwick.util.termdates;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.termdates.Term.TermType;

public final class TermFactoryImpl implements TermFactory {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("ddMMyy");
    
    private LinkedList<Term> termDates;
    
    public TermFactoryImpl() throws IOException {
        String source = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("termdates.txt")));
        this.termDates = new LinkedList<Term>();
        
        for (StringTokenizer st = new StringTokenizer(source, "\n"); st.hasMoreTokens();) {
            String line = st.nextToken().trim();
            String[] data = line.split(";");
            String startDateString = data[0];
            String endDateString = data[1];
            String termTypeString = data[2];
            
            DateTime startDate = DATE_FORMATTER.parseDateTime(startDateString);
            DateTime endDate = DATE_FORMATTER.parseDateTime(endDateString);
            TermType type;
            
            if (termTypeString.equals("a")) {
                type = TermType.autumn;
            } else if (termTypeString.equals("sp")) {
                type = TermType.spring;
            } else if (termTypeString.equals("su")) {
                type = TermType.summer;
            } else {
                throw new IllegalArgumentException("Invalid term string");
            }
            
            termDates.add(new TermImpl(this, startDate,endDate,type));
        }
    }

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

    public LinkedList<Term> getTermDates() {
        return termDates;
    }

}
