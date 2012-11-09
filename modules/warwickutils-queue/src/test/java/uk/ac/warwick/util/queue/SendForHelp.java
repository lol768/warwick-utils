package uk.ac.warwick.util.queue;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;

import uk.ac.warwick.util.queue.conversion.ItemType;

/**
 * Test message type for a test queue.
 */
@ItemType("SendForHelp")
@JsonAutoDetect
public class SendForHelp {
    private String message;
    private String to;
    private String from;
    
    private String transientData;
    
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    
    // This annotation has to be on a method.
    @JsonIgnore
    public String getTransientData() {
        return transientData;
    }
    public void setTransientData(String transientData) {
        this.transientData = transientData;
    }
    
}
