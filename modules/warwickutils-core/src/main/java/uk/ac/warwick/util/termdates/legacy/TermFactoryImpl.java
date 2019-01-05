package uk.ac.warwick.util.termdates.legacy;

import com.google.common.collect.Lists;
import org.threeten.extra.LocalDateRange;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.core.StringUtils;
import uk.ac.warwick.util.termdates.legacy.Term.TermType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @deprecated Use {@link uk.ac.warwick.util.termdates.AcademicYear}
 */
@Deprecated
public final class TermFactoryImpl implements TermFactory {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");
    
    private LinkedList<Term> termDates;
    
    public TermFactoryImpl() throws IOException {
        String source = StringUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("/uk/ac/warwick/util/termdates/termdates.txt")));
        this.termDates = new LinkedList<>();
        
        for (StringTokenizer st = new StringTokenizer(source, "\n"); st.hasMoreTokens();) {
            String line = st.nextToken().trim();
            String[] data = line.split(";");
            String startDateString = data[0];
            String endDateString = data[1];
            String termTypeString = data[2];
            
            LocalDate startDate = LocalDate.parse(startDateString, DATE_FORMATTER).with(DayOfWeek.MONDAY);
            LocalDate endDate = LocalDate.parse(endDateString, DATE_FORMATTER).with(DayOfWeek.SUNDAY);

            TermType type;

            switch (termTypeString) {
                case "a":
                    type = TermType.autumn;
                    break;
                case "sp":
                    type = TermType.spring;
                    break;
                case "su":
                    type = TermType.summer;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid term string");
            }
            
            termDates.add(new TermImpl(this, startDate, endDate, type));
        }
    }

    public Term getTermFromDate(final Temporal temporal) throws TermNotFoundException {
        LocalDate date = LocalDate.from(temporal);
        for (Term term: termDates) {
            if (date.isEqual(term.getEndDate()) || date.isBefore(term.getEndDate())) {
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
    
    public LocalDateRange getAcademicWeek(Temporal date, int weekNumber) throws TermNotFoundException {
        List<Pair<Integer, LocalDateRange>> weeks = getAcademicWeeksForYear(date);
        for (Pair<Integer, LocalDateRange> week : weeks) {
            if (week.getLeft() == weekNumber) {
                return week.getRight();
            }
        }
        
        throw new TermNotFoundException("Couldn't find interval for academic week");
    }
    
    public List<Pair<Integer, LocalDateRange>> getAcademicWeeksForYear(Temporal date) throws TermNotFoundException {
        List<Pair<Integer, LocalDateRange>> weeks = Lists.newArrayList();
        
        // Roll back to autumn term
        Term autumnTerm = getTermFromDate(date);
        while (autumnTerm.getTermType() != TermType.autumn) {
            autumnTerm = getPreviousTerm(autumnTerm);
        }

        Term nextYearAutumnTerm = autumnTerm;
        do {
            nextYearAutumnTerm = getNextTerm(nextYearAutumnTerm);
        } while (nextYearAutumnTerm.getTermType() != TermType.autumn);
        
        LocalDate dt = autumnTerm.getStartDate().with(DayOfWeek.MONDAY);
        int weekNumber = autumnTerm.getAcademicWeekNumber(dt);
        while (weekNumber > 0 && dt.isBefore(nextYearAutumnTerm.getStartDate())) {
            LocalDate start = dt;
            dt = dt.plusWeeks(1);
            LocalDate end = dt;

            weekNumber = autumnTerm.getAcademicWeekNumber(start);

            if (weekNumber > 0) {
                weeks.add(Pair.of(weekNumber, LocalDateRange.of(start, end)));
            }
        }
        
        return weeks;
    }

    public void setTermDates(final LinkedList<Term> termDates) {
        this.termDates = termDates;
    }

    public LinkedList<Term> getTermDates() {
        return termDates;
    }

}
