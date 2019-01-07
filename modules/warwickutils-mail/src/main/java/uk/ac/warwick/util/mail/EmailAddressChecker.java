package uk.ac.warwick.util.mail;

import org.apache.commons.validator.routines.EmailValidator;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import uk.ac.warwick.util.core.StringUtils;

public final class EmailAddressChecker {   
    private static final EmailValidator VALIDATOR = EmailValidator.getInstance();

    private boolean empty;
    private boolean matches;
    
    public EmailAddressChecker(final String email) {
        if (!StringUtils.hasText(email)) {
            empty = true;
            matches = false;
        } else {
            empty = false;
            
            if (VALIDATOR.isValid(email)) {
                try {
                    String domain = email.substring(email.lastIndexOf("@") + 1).trim();
                    Record[] records = new Lookup(domain, Type.MX).run();
                    // if no MX record, try looking for an A record
                    // SBTWO-5275, re: standard for SMTP - RFC-2821
                    if (records == null) {
                        records = new Lookup(domain, Type.A).run();
                    }
                   
                    matches = (records != null);
                } catch (TextParseException e) {
                    matches = false;
                }
            }
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isValid() {
        return (!empty && matches);
    }

    public boolean isMatches() {
        return matches;
    }
}
