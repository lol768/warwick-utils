package uk.ac.warwick.util.queue.conversion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * When defining a JSON mapping for a queueable object, this
 * annotation is required to tell it what type string to attach
 * to the message.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemType {
    String value();
}
