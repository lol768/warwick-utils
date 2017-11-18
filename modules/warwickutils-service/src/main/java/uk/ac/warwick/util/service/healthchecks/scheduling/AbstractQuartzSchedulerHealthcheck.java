package uk.ac.warwick.util.service.healthchecks.scheduling;

import uk.ac.warwick.util.core.scheduling.QuartzDAO;
import uk.ac.warwick.util.core.scheduling.QuartzScheduler;
import uk.ac.warwick.util.service.ServiceHealthcheck;
import uk.ac.warwick.util.service.ServiceHealthcheck.Status;
import uk.ac.warwick.util.service.ServiceHealthcheckProvider;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collection;

import static java.lang.Math.*;
import static java.time.LocalDateTime.*;
import static java.util.Arrays.*;

@Singleton
public abstract class AbstractQuartzSchedulerHealthcheck extends ServiceHealthcheckProvider {

    private static final String DEFAULT_NAME = "quartz-schedulers";

    private static final int DEFAULT_MINIMUM_SCHEDULERS = 2;

    private final QuartzDAO dao;
    private final Provider<String> schedulerNameProvider;

    private final String name;

    private int minimumSchedulers = DEFAULT_MINIMUM_SCHEDULERS;

    public AbstractQuartzSchedulerHealthcheck(String name, QuartzDAO dao, Provider<String> schedulerNameProvider) {
        super(new ServiceHealthcheck(
            name,
            Status.Unknown,
            now()
        ));

        this.name = name;
        this.dao = dao;
        this.schedulerNameProvider = schedulerNameProvider;
    }

    public AbstractQuartzSchedulerHealthcheck(QuartzDAO dao, Provider<String> schedulerNameProvider) {
        this(DEFAULT_NAME, dao, schedulerNameProvider);
    }

    protected final ServiceHealthcheck status() {
        final Status status;
        final String scheduler = schedulerNameProvider.get();

        Collection<QuartzScheduler> schedulers = dao.getSchedulers(scheduler);

        int totalSchedulers = schedulers.size();
        int staleSchedulers = toIntExact(schedulers.stream().filter(QuartzScheduler::isStale).count());
        int activeSchedulers = totalSchedulers - staleSchedulers;

        if (totalSchedulers < minimumSchedulers || activeSchedulers == 0) {
            status = Status.Error;
        } else if (activeSchedulers < minimumSchedulers) {
            status = Status.Warning;
        } else {
            status = Status.Okay;
        }

        final String message;
        if (staleSchedulers > 0) {
            message = totalSchedulers + (totalSchedulers == 1 ? " scheduler" : " schedulers") + " in cluster " + scheduler + ", " + staleSchedulers + " stale";
        } else {
            message = totalSchedulers + (totalSchedulers == 1 ? " scheduler" : " schedulers") + " in cluster " + scheduler;
        }

        return new ServiceHealthcheck(
            name,
            status,
            now(),
            message,
            asList(
                new ServiceHealthcheck.PerformanceData<>(
                    "scheduler_count", totalSchedulers, minimumSchedulers, minimumSchedulers
                ),
                new ServiceHealthcheck.PerformanceData<>(
                    "active_scheduler_count", activeSchedulers, minimumSchedulers, 0
                )
            )
        );
    }

    public void setMinimumSchedulers(int minimumSchedulers) {
        this.minimumSchedulers = minimumSchedulers;
    }
}
