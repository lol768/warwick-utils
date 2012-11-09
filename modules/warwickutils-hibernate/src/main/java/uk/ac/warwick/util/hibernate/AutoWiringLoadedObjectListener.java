package uk.ac.warwick.util.hibernate;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import uk.ac.warwick.util.hibernate.AnnotationFilteringEventListener.LoadedObjectListener;
import uk.ac.warwick.util.hibernate.AnnotationFilteringEventListener.SavedOrUpdatedObjectListener;

/**
 * @requires Spring
 */
public final class AutoWiringLoadedObjectListener implements LoadedObjectListener, SavedOrUpdatedObjectListener, BeanFactoryAware  {
    private AutowireCapableBeanFactory beanFactory;

    public void loaded(final Object loadedObject) {
        autowire(loadedObject);
    }

    public void savedOrUpdated(Object savedOrUpdatedObject) {
        autowire(savedOrUpdatedObject);
    }
    
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
    }
    
    private void autowire(final Object object) {
        beanFactory.autowireBeanProperties(object, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
}
