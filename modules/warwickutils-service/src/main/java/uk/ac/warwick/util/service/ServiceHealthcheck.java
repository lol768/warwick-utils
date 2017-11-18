package uk.ac.warwick.util.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.warwick.util.core.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServiceHealthcheck {

    public enum Status {
        Okay("okay"),
        Warning("warning"),
        Error("error"),
        Unknown("unknown");

        private String asString;

        Status(String asString) {
            this.asString = asString;
        }

        public String asString() {
            return asString;
        }
    }

    private final String name;

    private final Status status;

    private final LocalDateTime testedAt;

    private final String message;

    private final List<PerformanceData<?>> performanceData;

    public ServiceHealthcheck(String name, Status status, LocalDateTime testedAt) {
        this(name, status, testedAt, "", Collections.emptyList());
    }

    public ServiceHealthcheck(String name, Status status, LocalDateTime testedAt, String message) {
        this(name, status, testedAt, message, Collections.emptyList());
    }

    public ServiceHealthcheck(String name, Status status, LocalDateTime testedAt, List<PerformanceData<?>> performanceData) {
        this(name, status, testedAt, "", performanceData);
    }

    public ServiceHealthcheck(String name, Status status, LocalDateTime testedAt, String message, List<PerformanceData<?>> performanceData) {
        this.name = name;
        this.status = status;
        this.testedAt = testedAt;
        this.message = message;
        this.performanceData = Collections.unmodifiableList(performanceData);
    }

    public Map<String, Object> asJson() {
        ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();

        json.put("name", name)
            .put("status", status.asString())
            .put("testedAt",
                testedAt.atZone(ZoneId.of("Europe/London"))
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_DATE_TIME)
            );

        ImmutableList.Builder<String> perfData = ImmutableList.builder();
        for (PerformanceData<?> data : performanceData) {
            perfData.add(data.asString());
        }
        json.put("perfData", perfData.build());

        if (StringUtils.hasText(message)) {
            json.put("message", message);
        }

        return json.build();
    }

    public static class PerformanceData<T> {

        private final String name;

        private final T value;

        private final Optional<T> warningThreshold;

        private final Optional<T> errorThreshold;

        private final Optional<T> minimumValue;

        private final Optional<T> maximumValue;

        public PerformanceData(String name, T value) {
            this.name = name;
            this.value = value;
            this.warningThreshold = Optional.empty();
            this.errorThreshold = Optional.empty();
            this.minimumValue = Optional.empty();
            this.maximumValue = Optional.empty();
        }

        public PerformanceData(String name, T value, T warningThreshold, T errorThreshold) {
            this.name = name;
            this.value = value;
            this.warningThreshold = Optional.of(warningThreshold);
            this.errorThreshold = Optional.of(errorThreshold);
            this.minimumValue = Optional.empty();
            this.maximumValue = Optional.empty();
        }

        public PerformanceData(String name, T value, T warningThreshold, T errorThreshold, T minimumValue, T maximumValue) {
            this.name = name;
            this.value = value;
            this.warningThreshold = Optional.of(warningThreshold);
            this.errorThreshold = Optional.of(errorThreshold);
            this.minimumValue = Optional.of(minimumValue);
            this.maximumValue = Optional.of(maximumValue);
        }

        public String asString() {
            if (warningThreshold.isPresent() && errorThreshold.isPresent()) {
                if (minimumValue.isPresent() && maximumValue.isPresent()) {
                    return String.format(
                        "%s=%s;%s;%s;%s;%s",
                        name, value, warningThreshold.get(), errorThreshold.get(), minimumValue.get(), maximumValue.get()
                    );
                } else {
                    return String.format(
                        "%s=%s;%s;%s",
                        name, value, warningThreshold.get(), errorThreshold.get()
                    );
                }
            } else {
                return String.format(
                    "%s=%s",
                    name, value
                );
            }
        }
    }

}
