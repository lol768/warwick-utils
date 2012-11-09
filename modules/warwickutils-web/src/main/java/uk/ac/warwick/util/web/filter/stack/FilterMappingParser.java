package uk.ac.warwick.util.web.filter.stack;

public interface FilterMappingParser {

    boolean matches(String requestPath, String mapping);

}