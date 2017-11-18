package uk.ac.warwick.util.core.scheduling;

import java.util.Collection;

public interface QuartzDAO {
    int countTriggerErrors(String schedulerName);
    Collection<QuartzTrigger> getPendingTriggers(String schedulerName);
    Collection<QuartzScheduler> getSchedulers(String schedulerName);
}
