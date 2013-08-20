package uk.ac.warwick.util.hibernate4;


import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;

/**
 * Base for user type tests. Constructs a mock SessionImplementor that does
 * just enough. You can tell JMock to make the mock return further mocks, but
 * then the ValueBinder and ValueExtractor objects are mocks that do nothing,
 * so we need to pop in a real Dialect so that it actually does work.
 *
 * It doesn't really matter what the Dialect is but I chose HSQLDialect.
 */
public abstract class UserTypeTestBase {

    protected final Mockery m = new JUnit4Mockery();
    protected final SessionImplementor sessionImplementor = m.mock(SessionImplementor.class);

    private final SessionFactoryImplementor factory = m.mock(SessionFactoryImplementor.class);

    @Before
    public void setup() {
        m.checking(new Expectations(){{
            allowing(sessionImplementor).getFactory(); will(returnValue(factory));
            allowing(factory).getDialect(); will(returnValue(new HSQLDialect()));
            ignoring(sessionImplementor);
            ignoring(factory);
        }});
    }

}
