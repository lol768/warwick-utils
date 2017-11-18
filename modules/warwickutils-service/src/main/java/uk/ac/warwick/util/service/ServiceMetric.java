package uk.ac.warwick.util.service;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

public class ServiceMetric<T extends Number> {

    private final String name;

    private final Optional<T> total;

    private final Optional<T> current;

    private final Optional<T> last1Min;

    private final Optional<T> last5Min;

    private final Optional<T> last15Min;

    private final ImmutableMap<String, ServiceMetric<?>> nested;

    public ServiceMetric(String name, T total, T last1Min, T last5Min, T last15Min) {
        this(name, total, last1Min, last5Min, last15Min, ImmutableMap.of());
    }

    public ServiceMetric(String name, T total, T last1Min, T last5Min, T last15Min, Map<String, ServiceMetric<?>> nested) {
        this.name = name;
        this.total = Optional.of(total);
        this.current = Optional.empty();
        this.last1Min = Optional.of(last1Min);
        this.last5Min = Optional.of(last5Min);
        this.last15Min = Optional.of(last15Min);
        this.nested = ImmutableMap.copyOf(nested);
    }

    public ServiceMetric(String name, T total, T current, T last1Min, T last5Min, T last15Min) {
        this(name, total, current, last1Min, last5Min, last15Min, ImmutableMap.of());
    }

    public ServiceMetric(String name, T total, T current, T last1Min, T last5Min, T last15Min, Map<String, ServiceMetric<?>> nested) {
        this.name = name;
        this.total = Optional.of(total);
        this.current = Optional.of(current);
        this.last1Min = Optional.of(last1Min);
        this.last5Min = Optional.of(last5Min);
        this.last15Min = Optional.of(last15Min);
        this.nested = ImmutableMap.copyOf(nested);
    }

    public ServiceMetric(String name, Map<String, ServiceMetric<?>> nested) {
        this.name = name;
        this.total = Optional.empty();
        this.current = Optional.empty();
        this.last1Min = Optional.empty();
        this.last5Min = Optional.empty();
        this.last15Min = Optional.empty();
        this.nested = ImmutableMap.copyOf(nested);
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> asJson() {
        ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();

        if (current.isPresent()) { json.put("current", current.get()); }
        if (total.isPresent()) { json.put("total", total.get()); }
        if (last1Min.isPresent()) { json.put("01min", last1Min.get()); }
        if (last5Min.isPresent()) { json.put("05min", last5Min.get()); }
        if (last15Min.isPresent()) { json.put("15min", last15Min.get()); }

        for (Map.Entry<String, ServiceMetric<?>> entry : nested.entrySet()) {
            json.put(entry.getKey(), entry.getValue().asJson());
        }

        return json.build();
    }

}
