package uk.ac.warwick.util;

import static org.junit.Assert.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import uk.ac.warwick.userlookup.User;

/**
 * A place to put code that might be shared between multiple tests using mock objects,
 * such as defining expectations on an object, or returning an Action to use as a side-effect
 * when a method is called on a mocked object.
 */
public final class MockUtils {
    private MockUtils(){}
     
    /**
     * Applied as an action on Filter.doFilter(req,res,chain). It will call
     * chain.doFilter(req,res).
     * <p>
     * <h3>Example</h3>
     * <pre>
     * one(myFilter).doFilter(req,res,ch); will(continueFilterChain());
     * </pre>
     */
    public static Action continueFilterChain() {
        return new Action() {
            public Object invoke(Invocation invocation) throws Throwable {
                Object[] p = invocation.getParametersAsArray();
                try {
                    ServletRequest req = (ServletRequest) p[0];
                    ServletResponse res = (ServletResponse) p[1];
                    FilterChain chain = (FilterChain)p[2];
                    chain.doFilter(req, res);
                } catch (ClassCastException e) {
                    fail("Unexpected class cast exception - is this action applied to doFilter()?");
                } catch (ArrayIndexOutOfBoundsException e) {
                    fail("Not enough method arguments - is this action applied to doFilter()?");
                }
                return null;
            }
            
            public void describeTo(Description d) {
                d.appendText("continue filter chain on doFilter");
            }
        };
    }
    
    /**
     * Sets an attribute on the first argument which should be a ServletRequest.
     */
    public static Action setAttribute(final String name, final String value) {
        return new Action() {
            public Object invoke(Invocation invocation) throws Throwable {
                ServletRequest req = (ServletRequest) invocation.getParameter(0);
                req.setAttribute(name, value);
                return null;
            }
            
            public void describeTo(Description d) {
                d.appendText("Set request attribute ");
                d.appendValue(name);
                d.appendText(" to ");
                d.appendValue(value);
            }
        };
    }
    
    /**
     * Asserts the existence of a request attribute from the ServletRequest
     * in the first argument. the expects parameter denotes whether we expect
     * it to exist, so we can check for the nonexistence of an attribute too.
     */
    public static Action checkAttribute(final String name, final boolean expects) {
        return new Action() {
            public Object invoke(Invocation invocation) throws Throwable {
                ServletRequest req = (ServletRequest) invocation.getParameter(0);
                assertEquals(expects, req.getAttribute(name) != null);
                return null;
            }
            
            public void describeTo(Description d) {
                d.appendText("Check that request attribute ");
                d.appendValue(name);
                d.appendText(" existence is ");
                d.appendValue(expects);
            }
        };
    }
    
    /**
     * JMock Action to return the user given as ID in the first parameter. eg:
     * <pre>
     * allowing(userLookup).findUserById(with(any(String.class))); will(returnUser());
     * </pre>
     */
    public static Action returnUser() {
        return new Action() {
            public Object invoke(Invocation invocation) throws Throwable {
                User u = new User((String) invocation.getParameter(0));
                u.setFoundUser(true);
                return u;
            }
            public void describeTo(Description desc) {
                desc.appendText("return User object of provided userId");
            }
        };
    }
}
