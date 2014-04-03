package uk.ac.warwick.util.termdates;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.joda.time.base.BaseDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.termdates.Term.TermType;

import com.google.common.collect.Lists;

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
            
            MutableDateTime startDate = DATE_FORMATTER.parseDateTime(startDateString).withDayOfWeek(DateTimeConstants.MONDAY).toMutableDateTime();
            MutableDateTime endDate = DATE_FORMATTER.parseDateTime(endDateString).withDayOfWeek(DateTimeConstants.SUNDAY).toMutableDateTime();

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
            
            termDates.add(new TermImpl(this, startDate.toDateTime(),endDate.toDateTime(),type));
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
    
    public Interval getAcademicWeek(BaseDateTime date, int weekNumber) throws TermNotFoundException {
        List<Pair<Integer, Interval>> weeks = getAcademicWeeksForYear(date);
        for (Pair<Integer, Interval> week : weeks) {
            if (week.getLeft() == weekNumber) {
                return week.getRight();
            }
        }
        
        throw new TermNotFoundException("Couldn't find interval for academic week");
    }
    
    public List<Pair<Integer, Interval>> getAcademicWeeksForYear(BaseDateTime date) throws TermNotFoundException {
        List<Pair<Integer, Interval>> weeks = Lists.newArrayList();
        
        // Roll back to autumn term
        Term autumnTerm = getTermFromDate(date);
        while (autumnTerm.getTermType() != TermType.autumn) {
            autumnTerm = getPreviousTerm(autumnTerm);
        }
        
        MutableDateTime dt = autumnTerm.getStartDate().withMillisOfDay(0).withDayOfWeek(DateTimeConstants.MONDAY).toMutableDateTime();
        int weekNumber = autumnTerm.getAcademicWeekNumber(dt);
        while (weekNumber > 0) {            
            DateTime start = dt.toDateTime();
            dt.addWeeks(1);
            DateTime end = dt.toDateTime();
            
            weeks.add(Pair.of(weekNumber, new Interval(start, end)));
            weekNumber = autumnTerm.getAcademicWeekNumber(dt);
        };
        
        return weeks;
    }

    public void setTermDates(final LinkedList<Term> termDates) {
        this.termDates = termDates;
    }

    public LinkedList<Term> getTermDates() {
        return termDates;
    }

}
