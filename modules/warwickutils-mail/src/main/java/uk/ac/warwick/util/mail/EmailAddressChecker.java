package uk.ac.warwick.util.mail;

import org.apache.commons.validator.routines.EmailValidator;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import uk.ac.warwick.util.core.StringUtils;

import static org.xbill.DNS.Lookup.TRY_AGAIN;
import static org.xbill.DNS.Lookup.UNRECOVERABLE;

public final class EmailAddressChecker {   
    private static final EmailValidator VALIDATOR = EmailValidator.getInstance();

    private boolean empty;
    private boolean matches;
    private boolean serverError = false;
    
    public EmailAddressChecker(final String email) {
        if (!StringUtils.hasText(email)) {
            empty = true;
            matches = false;
        } else {
            empty = false;
            
            if (VALIDATOR.isValid(email)) {
                try {
                    String domain = email.substring(email.lastIndexOf("@") + 1).trim();
                    Lookup mxLookup = new Lookup(domain, Type.MX);
                    Record[] records = mxLookup.run();
                    handleServerError(mxLookup);
                    // if no MX record, try looking for an A record
                    // SBTWO-5275, re: standard for SMTP - RFC-2821
                    if (records == null) {
                        Lookup aLookup = new Lookup(domain, Type.A);
                        records = aLookup.run();
                        handleServerError(aLookup);
                    }
                   
                    matches = (records != null);
                } catch (TextParseException e) {
                    matches = false;
                }
            }
        }
    }

    private void handleServerError(Lookup mxLookup) {
        if (mxLookup.getResult() == TRY_AGAIN || mxLookup.getResult() == UNRECOVERABLE) {
            serverError = true;
        }
    }

    /**
     * @return If string is null/consists solely of whitespace.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @return If a) non-empty and b) a valid DNS record
     * exists (or a server error prevented lookup).
     */
    public boolean isValid() {
        return (!empty && matches) || (!empty && serverError);
    }

    /**
     * @return If a valid DNS record exists.
     */
    public boolean isMatches() {
        return matches;
    }
}
