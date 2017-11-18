package uk.ac.warwick.util.service;

public abstract class ServiceHealthcheckProvider {

    private ServiceHealthcheck latest;

    protected ServiceHealthcheckProvider(ServiceHealthcheck initialState) {
        this.latest = initialState;
    }

    protected void update(ServiceHealthcheck results) {
        latest = results;
    }

    protected abstract void run();

    public ServiceHealthcheck latest() {
        return latest;
    }

}
