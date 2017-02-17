package uk.ac.warwick.util.logging;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * <p>An audit logger which standardises logging audit actions from any system. This
 * will then get picked up by our standard Logback configurations and sent to the
 * relevant locations, i.e. CLogS as well as an audit.log.yyyy-MM-dd</p>
 *
 * <p>Use {@link AuditLogger#getAuditLogger(String applicationKey)} to get an instance
 * of the audit logger for your application; the application key must be a simple string
 * as it's used as the object key in logging for application-specific data.</p>
 */
public class AuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("uk.ac.warwick.AUDIT");

    /**
     * A valid field to get logged. This would be an enum to prevent people inventing
     * new field names willy nilly, _but_ it is occasionally useful to be able to
     * define arbitrary ones.
     * <p>
     * Before adding a new field name, check whether it already exists in the logging
     * system or in other apps. Having common field names is vital to being able to
     * do useful searches.
     */
    public static final class Field {
        // standard fields - not to be used in data() args
        private static final Field eventType = new Field("event_type");
        private static final Field username = new Field("username");
        private static final Field ipAddress = new Field("source_ip");

        private String value;

        public Field(@NotNull String value) {
            if (value == null) throw new IllegalArgumentException();
            this.value = value;
        }

        /** The field name that will be sent to Logstash. */
        public String getValue() {
            return value;
        }
        @Override public boolean equals(Object other) {
            return other != null && other instanceof Field && ((Field)other).value.equals(this.value);
        }
    }

    private final String applicationKey;

    private AuditLogger(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public void log(RequestInformation info) {
        log(info, null);
    }

    public void log(RequestInformation info, Map<Field, Object> data) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(Field.eventType.getValue(), info.eventType);

        if (info.username != null) map.put(Field.username.getValue(), info.username);
        if (info.ipAddress != null) map.put(Field.ipAddress.getValue(), info.ipAddress);

        if (info.userAgent != null)
            map.put("request_headers", new LinkedHashMap<String, Object>() {{
                put("user-agent", info.userAgent);
            }});

        if (data != null)
            // remap keys to strings
            map.put(applicationKey, data.entrySet().stream()
                .collect(toMap(
                    e -> e.getKey().getValue(),
                    Map.Entry::getValue // value stays the same
                )));

        LOGGER.info("{}", StructuredArguments.entries(map));
    }

    public static AuditLogger getAuditLogger(String applicationKey) {
        if (!applicationKey.matches("^[a-z]+$")) {
            throw new IllegalArgumentException("Application key must be all lowercase characters");
        }

        return new AuditLogger(applicationKey);
    }

    public static class RequestInformation {

        private final String eventType;

        private String username;

        private String userAgent;

        private String ipAddress;

        private RequestInformation(String eventType) {
            this.eventType = eventType;
        }

        public static RequestInformation forEventType(String eventType) {
            return new RequestInformation(eventType);
        }

        public RequestInformation withUsername(@NotNull String username) {
            if (username == null) throw new IllegalArgumentException();
            this.username = username;
            return this;
        }

        public RequestInformation withUserAgent(@NotNull String userAgent) {
            if (userAgent == null) throw new IllegalArgumentException();
            this.userAgent = userAgent;
            return this;
        }

        public RequestInformation withIpAddress(@NotNull String ipAddress) {
            if (ipAddress == null) throw new IllegalArgumentException();
            this.ipAddress = ipAddress;
            return this;
        }

    }

}
