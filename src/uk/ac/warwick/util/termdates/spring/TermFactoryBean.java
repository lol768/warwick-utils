package uk.ac.warwick.util.termdates.spring;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.termdates.Term;
import uk.ac.warwick.util.termdates.TermImpl;
import uk.ac.warwick.util.termdates.Term.TermType;

public final class TermFactoryBean extends AbstractFactoryBean {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("ddMMyy");

    @Override
    public Object createInstance() throws Exception {
        String source = FileCopyUtils.copyToString(new InputStreamReader(getClass().getResourceAsStream("termdates.txt")));
        LinkedList<Term> terms = new LinkedList<Term>();
        
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
            
            terms.add(new TermImpl(startDate,endDate,type));
        }
        
        return terms;
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType() {
        return LinkedList.class;
    }

}
