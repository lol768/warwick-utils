package uk.ac.warwick.util.hibernate4;

import org.hibernate.event.spi.*;

import java.util.List;


public final class AnnotationFilteringEventListener implements PostLoadEventListener, PostInsertEventListener,
        PostUpdateEventListener, PostDeleteEventListener {
    public static interface ObjectListener {
    }

    public static interface LoadedObjectListener extends ObjectListener {
        void loaded(final Object loadedObject);
    }

    public static interface SavedOrUpdatedObjectListener extends ObjectListener {
        void savedOrUpdated(final Object savedOrUpdatedObject);
    }

    public static interface DeletedObjectListener extends ObjectListener {
        void deleted(final Object deletedObject);
    }

    private static final long serialVersionUID = 8652156835267250103L;

    //private static final Logger LOGGER = Logger.getLogger(AnnotationFilteringEventListener.class);

    private final List<ObjectListener> listeners;

    private final Class<?> annotationClass;

    public AnnotationFilteringEventListener(final String clazz, final List<ObjectListener> theListeners)
            throws ClassNotFoundException {
        this.annotationClass = Class.forName(clazz);
        this.listeners = theListeners;
    }

    public void onPostLoad(final PostLoadEvent event) {
        Object loadedObject = event.getEntity();
        handleLoadedObject(loadedObject);
    }

    public void onPostInsert(PostInsertEvent event) {
        Object savedObject = event.getEntity();
        handleSavedOrUpdatedObject(savedObject);
    }

    public void onPostUpdate(PostUpdateEvent event) {
        Object savedObject = event.getEntity();
        handleSavedOrUpdatedObject(savedObject);
    }

    public void onPostDelete(PostDeleteEvent event) {
        Object deletedObject = event.getEntity();
        handleDeletedObject(deletedObject);
    }

    void handleLoadedObject(final Object object) {
        boolean objectHasAnnotation = objectHasAnnotation(object.getClass());
       // LOGGER.debug("Class " + object.getClass().getName() + " has annotation: " + objectHasAnnotation);
        if (objectHasAnnotation) {
            for (ObjectListener listener: listeners) {
                if (listener instanceof LoadedObjectListener) {
                    ((LoadedObjectListener) listener).loaded(object);
                }
            }
        }
    }

    void handleDeletedObject(final Object object) {
        boolean objectHasAnnotation = objectHasAnnotation(object.getClass());
        //LOGGER.debug("Class " + object.getClass().getName() + " has annotation: " + objectHasAnnotation);
        if (objectHasAnnotation) {
            for (ObjectListener listener: listeners) {
                if (listener instanceof DeletedObjectListener) {
                    ((DeletedObjectListener) listener).deleted(object);
                }
            }
        }
    }

    void handleSavedOrUpdatedObject(final Object object) {
        boolean objectHasAnnotation = objectHasAnnotation(object.getClass());
        //LOGGER.debug("Class " + object.getClass().getName() + " has annotation: " + objectHasAnnotation);
        if (objectHasAnnotation) {
            for (ObjectListener listener: listeners) {
                if (listener instanceof SavedOrUpdatedObjectListener) {
                    ((SavedOrUpdatedObjectListener) listener).savedOrUpdated(object);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean objectHasAnnotation(final Class loadedClass) {
        return loadedClass.getAnnotation(annotationClass) != null;
    }
}
