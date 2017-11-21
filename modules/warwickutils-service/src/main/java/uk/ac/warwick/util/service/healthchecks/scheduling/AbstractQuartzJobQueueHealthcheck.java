package uk.ac.warwick.util.service.healthchecks.scheduling;

import uk.ac.warwick.util.core.scheduling.QuartzDAO;
import uk.ac.warwick.util.core.scheduling.QuartzTrigger;
import uk.ac.warwick.util.service.ServiceHealthcheck;
import uk.ac.warwick.util.service.ServiceHealthcheck.Status;
import uk.ac.warwick.util.service.ServiceHealthcheckProvider;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Collection;

import static java.time.LocalDateTime.*;
import static java.util.Arrays.*;
import static java.util.Comparator.*;

@Singleton
public abstract class AbstractQuartzJobQueueHealthcheck extends ServiceHealthcheckProvider {

    private static final String DEFAULT_NAME = "quartz-job-queue";

    private static final Duration DEFAULT_AGE_CRITICAL_THRESHOLD = Duration.ofHours(6);

    private static final Duration DEFAULT_AGE_WARNING_THRESHOLD = Duration.ofHours(1);

    private static final int DEFAULT_SIZE_CRITICAL_THRESHOLD = 300;

    private static final int DEFAULT_SIZE_WARNING_THRESHOLD = 75;

    private final QuartzDAO dao;
    private final Provider<String> schedulerNameProvider;

    private final String name;
    private Duration ageCriticalThreshold = DEFAULT_AGE_CRITICAL_THRESHOLD;
    private Duration ageWarningThreshold = DEFAULT_AGE_WARNING_THRESHOLD;
    private int sizeCriticalThreshold = DEFAULT_SIZE_CRITICAL_THRESHOLD;
    private int sizeWarningThreshold = DEFAULT_SIZE_WARNING_THRESHOLD;

    public AbstractQuartzJobQueueHealthcheck(String name, QuartzDAO dao, Provider<String> schedulerNameProvider) {
        super(new ServiceHealthcheck(
            name,
            Status.Unknown,
            now()
        ));

        this.name = name;
        this.dao = dao;
        this.schedulerNameProvider = schedulerNameProvider;
    }

    public AbstractQuartzJobQueueHealthcheck(QuartzDAO dao, Provider<String> schedulerNameProvider) {
        this(DEFAULT_NAME, dao, schedulerNameProvider);
    }

    protected final ServiceHealthcheck status() {
        final Status status;
        final String scheduler = schedulerNameProvider.get();

        Collection<QuartzTrigger> pendingTriggers = dao.getPendingTriggers(scheduler);
        int totalTriggerCount = pendingTriggers.size();
        int errorTriggerCount = dao.countTriggerErrors(scheduler);

        Duration age = pendingTriggers.stream()
            .sorted(comparing(QuartzTrigger::getAge).reversed())
            .findFirst()
            .map(trg -> Duration.ofMillis(trg.getAge()))
            .orElse(Duration.ZERO);

        if (errorTriggerCount > 0) {
            status = Status.Error;
        } else if (age.compareTo(ageCriticalThreshold) >= 0 || totalTriggerCount >= sizeCriticalThreshold) {
            status = Status.Error;
        } else if (age.compareTo(ageWarningThreshold) >= 0 || totalTriggerCount >= sizeWarningThreshold) {
            status = Status.Warning;
        } else {
            status = Status.Okay;
        }

        final String message;
        if (errorTriggerCount == 0 && age.isZero() && totalTriggerCount == 0) {
            message = "No outstanding jobs in queue";
        } else {
            message =
                (errorTriggerCount > 0 ? errorTriggerCount + (errorTriggerCount > 1 ? " jobs" : " job") + " in error; " : "") +
                    totalTriggerCount + (totalTriggerCount != 1 ? " jobs" : " job") + " in queue for cluster " + scheduler + " (warn: " + sizeWarningThreshold + ", crit: " + sizeCriticalThreshold + "); " +
                    "last job is " + age.toMinutes() + " minutes old (warn: " + ageWarningThreshold.toMinutes() + ", crit: " + ageCriticalThreshold.toMinutes() + ")";
        }

        return new ServiceHealthcheck(
            name,
            status,
            now(),
            message,
            asList(
                new ServiceHealthcheck.PerformanceData<>(
                    "mins_old", age.toMinutes(), ageWarningThreshold.toMinutes(), ageCriticalThreshold.toMinutes()
                ),
                new ServiceHealthcheck.PerformanceData<>(
                    "queue_depth", totalTriggerCount, sizeWarningThreshold, sizeCriticalThreshold
                ),
                new ServiceHealthcheck.PerformanceData<>(
                    "failed_jobs", errorTriggerCount, 1, 1
                )
            )
        );
    }

    public void setAgeCriticalThreshold(Duration ageCriticalThreshold) {
        this.ageCriticalThreshold = ageCriticalThreshold;
    }

    public void setAgeWarningThreshold(Duration ageWarningThreshold) {
        this.ageWarningThreshold = ageWarningThreshold;
    }

    public void setSizeCriticalThreshold(int sizeCriticalThreshold) {
        this.sizeCriticalThreshold = sizeCriticalThreshold;
    }

    public void setSizeWarningThreshold(int sizeWarningThreshold) {
        this.sizeWarningThreshold = sizeWarningThreshold;
    }
}
