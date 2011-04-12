package uk.ac.warwick.util.queue.conversion;

/**
 * Does nothing - just an object to wire in from a Spring context.
 */
public class TestServiceBean {
    private final String name;

    private TestServiceBean(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
