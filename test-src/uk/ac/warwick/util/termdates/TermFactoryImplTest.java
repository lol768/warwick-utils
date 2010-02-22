package uk.ac.warwick.util.termdates;

import static org.junit.Assert.*;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;

import uk.ac.warwick.util.core.jodatime.DateTimeUtils;

public final class TermFactoryImplTest {

    @Test
    public void constructor() throws Exception {
        TermFactoryImpl bean = new TermFactoryImpl();

        List<Term> dates = bean.getTermDates();

        Term thirdTerm = dates.get(2);
        DateTime thirdTermStart = thirdTerm.getStartDate();
        DateTime april23rd = new DateTime().withDate(2007, DateTimeConstants.APRIL, 23);

        assertTrue(DateTimeUtils.isSameDay(thirdTermStart, april23rd));

        assertEquals(1, thirdTerm.getWeekNumber(april23rd));
    }

}
