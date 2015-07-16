package uk.ac.warwick.util.web.spring.validator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public abstract class AbstractValidatorTest<T, V extends AbstractValidator<T>> {

    private V validator;

    /**
     * Create an instance of the target to be validated, setting any required
     * autowired fields in the process.
     */
    protected abstract T createTarget();

    /**
     * Create a new instance of the validator.
     */
    protected abstract V createValidator() throws InstantiationException, IllegalAccessException;

    @Before
    public void setUp() throws InstantiationException, IllegalAccessException {
        this.validator = createValidator();
    }

    @Test
    public void supportsClass() {
        T command = createTarget();
        assertTrue(validator.supports(command.getClass()));
    }

    @Test
    public void doesntSupportAllClasses() {
        assertFalse(validator.supports(Object.class));
    }

    public Errors getErrors(T command) {
        Errors errors = new BindException(command, "command");
        validator.validate(command, errors);
        return errors;
    }

    public void assertPasses(T command) {
        Errors errors = getErrors(command);
        assertFalse("Expected validation to pass: " + errors.toString(), errors.hasErrors());
    }

    public Errors assertFails(T command) {
        Errors errors = getErrors(command);
        assertTrue("Expected validation to fail", errors.hasErrors());
        
        return errors;
    }

    protected V getValidator() {
        return validator;
    }

}
