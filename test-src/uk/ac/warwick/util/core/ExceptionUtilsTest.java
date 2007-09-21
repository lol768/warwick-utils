package uk.ac.warwick.util.core;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.web.util.NestedServletException;

public class ExceptionUtilsTest extends TestCase {

	public void testTopException() {
		UnsupportedOperationException e = new UnsupportedOperationException("hello");
		assertEquals(e, ExceptionUtils.getInterestingThrowable(e, new Class[] {ServletException.class}));
	}
	
	public void testLowerException() {
		RuntimeException re = new RuntimeException("Honk!");
		UnsupportedOperationException e = new UnsupportedOperationException("hello", re);
		assertEquals(e, ExceptionUtils.getInterestingThrowable(e, new Class[] {ServletException.class}));
	}

	public void testIgnorableException() {
		RuntimeException re = new RuntimeException("Honk!");
		UnsupportedOperationException e = new UnsupportedOperationException("hello", re);
		assertEquals(re, ExceptionUtils.getInterestingThrowable(e, new Class[] {UnsupportedOperationException.class}));
	}
	
	public void testServletRootCause() {
		UnsupportedOperationException e = new UnsupportedOperationException("hello");
		NestedServletException se = new NestedServletException("Terrible", e);
		
		assertEquals(e, ExceptionUtils.getInterestingThrowable(se, new Class[] {ServletException.class}));
	}
}
