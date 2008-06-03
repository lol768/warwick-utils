package uk.ac.warwick.util.hibernate;

import java.util.Collections;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import uk.ac.warwick.util.hibernate.AnnotationFilteringEventListener.LoadedObjectListener;
import uk.ac.warwick.util.hibernate.AnnotationFilteringEventListener.ObjectListener;
import uk.ac.warwick.util.hibernate.annotations.AutowiredOnRehydration;

public final class AnnotationFilteringEventListenerTest extends MockObjectTestCase {
    public void testItWorks() throws Exception {
        @AutowiredOnRehydration
        class AutowiredClass {

        }
        class NotAutowiredClass {

        }

        AutowiredClass theAutowiredObject = new AutowiredClass();
        NotAutowiredClass theNonWiredObject = new NotAutowiredClass();

        Mock mockListener = mock(LoadedObjectListener.class);
        mockListener.expects(once()).method("loaded").with(eq(theAutowiredObject));
        ObjectListener listener = (LoadedObjectListener) mockListener.proxy();
        AnnotationFilteringEventListener eventListener = new AnnotationFilteringEventListener(AutowiredOnRehydration.class.getName(),
                Collections.singletonList(listener));

        eventListener.handleLoadedObject(theAutowiredObject);
        eventListener.handleLoadedObject(theNonWiredObject);
    }
}
