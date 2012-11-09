package uk.ac.warwick.util.web.view;

import static org.junit.Assert.*;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;

import com.google.common.collect.Maps;

public final class AlternateAjaxViewTest {
    
    private Mockery m = new JUnit4Mockery();
    
    @Test
    public void getsToNormalView() throws Exception {
        final View ajaxView = m.mock(View.class, "ajaxView");
        final View standardView = m.mock(View.class, "standardView");
        
        AlternateAjaxView view = new AlternateAjaxView();
        view.setAjaxView(ajaxView);
        view.setStandardView(standardView);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("get");
        
        m.checking(new Expectations() {{
            // assert that we call render on the standard view
            one(standardView).render(with(any(Map.class)), with(aNonNull(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
        }});
        
        Map<String, Object> map = Maps.newHashMap();
        view.render(map, request, new MockHttpServletResponse());
        
        assertTrue(map.containsKey("ajax"));
        assertFalse((Boolean)map.get("ajax"));
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void postsToNormalView() throws Exception {
        final View ajaxView = m.mock(View.class, "ajaxView");
        final View standardView = m.mock(View.class, "standardView");
        
        AlternateAjaxView view = new AlternateAjaxView();
        view.setAjaxView(ajaxView);
        view.setStandardView(standardView);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("post");
        
        m.checking(new Expectations() {{
            // assert that we call render on the standard view
            one(standardView).render(with(any(Map.class)), with(aNonNull(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
        }});
        
        Map<String, Object> map = Maps.newHashMap();
        view.render(map, request, new MockHttpServletResponse());
        
        assertTrue(map.containsKey("ajax"));
        assertFalse((Boolean)map.get("ajax"));
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getsToAjaxWithHeader() throws Exception {
        final View ajaxView = m.mock(View.class, "ajaxView");
        final View standardView = m.mock(View.class, "standardView");
        
        AlternateAjaxView view = new AlternateAjaxView();
        view.setAjaxView(ajaxView);
        view.setStandardView(standardView);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("get");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        
        m.checking(new Expectations() {{
            // assert that we call render on the standard view
            one(ajaxView).render(with(any(Map.class)), with(aNonNull(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
        }});
        
        Map<String, Object> map = Maps.newHashMap();
        view.render(map, request, new MockHttpServletResponse());
        
        assertTrue(map.containsKey("ajax"));
        assertTrue((Boolean)map.get("ajax"));
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void postsToAjaxWithHeader() throws Exception {
        final View ajaxView = m.mock(View.class, "ajaxView");
        final View standardView = m.mock(View.class, "standardView");
        
        AlternateAjaxView view = new AlternateAjaxView();
        view.setAjaxView(ajaxView);
        view.setStandardView(standardView);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("post");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        
        m.checking(new Expectations() {{
            // assert that we call render on the standard view
            one(ajaxView).render(with(any(Map.class)), with(aNonNull(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
        }});
        
        Map<String, Object> map = Maps.newHashMap();
        view.render(map, request, new MockHttpServletResponse());
        
        assertTrue(map.containsKey("ajax"));
        assertTrue((Boolean)map.get("ajax"));
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void getsToAjaxWithParameter() throws Exception {
        final View ajaxView = m.mock(View.class, "ajaxView");
        final View standardView = m.mock(View.class, "standardView");
        
        AlternateAjaxView view = new AlternateAjaxView();
        view.setAjaxView(ajaxView);
        view.setStandardView(standardView);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("get");
        request.setParameter("ajax", "true");
        
        m.checking(new Expectations() {{
            // assert that we call render on the standard view
            one(ajaxView).render(with(any(Map.class)), with(aNonNull(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
        }});
        
        Map<String, Object> map = Maps.newHashMap();
        view.render(map, request, new MockHttpServletResponse());
        
        assertTrue(map.containsKey("ajax"));
        assertTrue((Boolean)map.get("ajax"));
        
        m.assertIsSatisfied();
    }
    
    @Test
    public void postsToAjaxWithParameter() throws Exception {
        final View ajaxView = m.mock(View.class, "ajaxView");
        final View standardView = m.mock(View.class, "standardView");
        
        AlternateAjaxView view = new AlternateAjaxView();
        view.setAjaxView(ajaxView);
        view.setStandardView(standardView);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("post");
        request.setParameter("ajax", "true");
        
        m.checking(new Expectations() {{
            // assert that we call render on the standard view
            one(ajaxView).render(with(any(Map.class)), with(aNonNull(HttpServletRequest.class)), with(any(HttpServletResponse.class)));
        }});
        
        Map<String, Object> map = Maps.newHashMap();
        view.render(map, request, new MockHttpServletResponse());
        
        assertTrue(map.containsKey("ajax"));
        assertTrue((Boolean)map.get("ajax"));
        
        m.assertIsSatisfied();
    }

}
