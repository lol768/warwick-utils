package uk.ac.warwick.util.service;

public abstract class ServiceMetricProvider<T extends Number> {

    private ServiceMetric<T> latest;

    protected ServiceMetricProvider(ServiceMetric<T> initialState) {
        this.latest = initialState;
    }

    protected void update(ServiceMetric<T> results) {
        latest = results;
    }

    protected abstract void run();

    public ServiceMetric<T> get() {
        return latest;
    }

}
