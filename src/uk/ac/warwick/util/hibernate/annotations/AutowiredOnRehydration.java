package uk.ac.warwick.util.hibernate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Causes a class to be autowired when Hibernate creates it.
 * 
 * If you need autowiring in other places, look at Configurable and
 * Resource annotations.
 * 
 * There is now an Autowired annotation in Spring, which is for much the
 * same thing. But we use this annotation a lot still. Perhaps look into
 * replacing our autowire annotation support with Spring's built-in stuff.
 * 
 */
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface AutowiredOnRehydration {
}
