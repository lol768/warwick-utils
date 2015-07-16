package uk.ac.warwick.util.web.spring.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.View;

/**
 * <p>
 * A view which specifies two alternate views, one for when the
 * page is requested normally by the browser, and another for when
 * it is requested by a Prototype AJAX call. It works by checking
 * the "X-Requested-With" header which Prototype sets to XMLHttpRequest
 * for AJAX calls.
 * <p>
 * Useful to give a stripped-down version for AJAX, and a version with
 * layout for browser viewing.
 * 
 * You can also get the AJAX view by adding ajax=true to the parameters.
 * 
 * It will add a boolean called "ajax" to the model, so you can
 * check in your view whether the request was AJAX or not. Useful
 * if the two views share templates.
 * 
 * <pre><code>
 * myFormView.(class)=uk.ac.warwick.util.web.view.AlternateAjaxView
 * myFormView.standardView(ref)=myStandardFormView
 * myFormView.ajaxView(ref)=myAjaxFormView
 * </code></pre>
 */
public final class AlternateAjaxView implements View {

    private static final String EXPECTED_HEADER_NAME = "X-Requested-With";
    private static final String EXPECTED_HEADER_VALUE = "XMLHttpRequest";
    
    //alternative parameter if we want to use the ajax view without needing headers for some reason
    private static final String PARAMETER = "ajax";
    
    private View standardView;
    private View ajaxView;
    private String contentType = "text/html";
    
    @SuppressWarnings({"unchecked","rawtypes"})
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestedWith = request.getHeader(EXPECTED_HEADER_NAME);
        boolean override = ServletRequestUtils.getBooleanParameter(request, PARAMETER, false);
        boolean isAjax = override || (requestedWith != null && requestedWith.contains(EXPECTED_HEADER_VALUE));
        model.put("ajax", isAjax);
		if (isAjax) {
            ajaxView.render(model, request, response);
        } else {
            standardView.render(model, request, response);
        }
    }
    
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public View getStandardView() {
        return standardView;
    }

    public void setStandardView(View standardView) {
        if (standardView == this) {
            throw new IllegalArgumentException("View was passed to itself");
        }
        this.standardView = standardView;
    }

    public View getAjaxView() {
        return ajaxView;
    }

    public void setAjaxView(View ajaxView) {
        if (ajaxView == this) {
            throw new IllegalArgumentException("View was passed to itself");
        }
        this.ajaxView = ajaxView;
    }

}
