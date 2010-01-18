package uk.ac.warwick.util.web.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * I <em>will</em> have a generic validator!
 */
@SuppressWarnings("unchecked")
public abstract class AbstractValidator<T> implements Validator {
    
    private final Class<? extends T> clazz;
    
    public AbstractValidator(Class<? extends T> theClazz) {
        this.clazz = theClazz;
    }

    public final boolean supports(Class theClazz) {
        return clazz.isAssignableFrom(theClazz);
    }

    public final void validate(Object target, Errors errors) {
        doValidate((T)target, errors);
    }
    
    public abstract void doValidate(T command, Errors errors);

}
