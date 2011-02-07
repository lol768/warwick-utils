package uk.ac.warwick.util.web.filter;

public interface RequestIPAddressHolder {
    
    boolean hasNonLocalAddress();
    
    String getNonLocalAddress();

}
