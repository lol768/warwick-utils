package uk.ac.warwick.util.web;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import uk.ac.warwick.util.core.StringUtils;

public final class UriBean extends AbstractFactoryBean {
    
    private String externalForm;
    
    private Uri uri;

    @Override
    public Class<Uri> getObjectType() {
        return Uri.class;
    }

    @Override
    protected Uri createInstance() throws Exception {
        return uri;
    }

    @Override
    public void afterPropertiesSet() throws Exception {        
        if (!StringUtils.hasText(externalForm)) {
            throw new IllegalArgumentException("Must specify an external form for the URI");
        }
        
        // If this is invalid, the UriException comes as part of the initialisation
        this.uri = Uri.parse(externalForm);
        
        super.afterPropertiesSet();
    }

    public void setExternalForm(String externalForm) {
        this.externalForm = externalForm;
    }

}
