package uk.ac.warwick.util.core.scheduling;

import org.joda.time.DateTime;

public class QuartzScheduler {
    private String schedulerName;
    private String instanceName;
    private long checkinTimeInMillis;
    private int checkinIntervalInMillis;

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public long getCheckinTimeInMillis() {
        return checkinTimeInMillis;
    }

    public void setCheckinTimeInMillis(long checkinTimeInMillis) {
        this.checkinTimeInMillis = checkinTimeInMillis;
    }

    public int getCheckinIntervalInMillis() {
        return checkinIntervalInMillis;
    }

    public void setCheckinIntervalInMillis(int checkinIntervalInMillis) {
        this.checkinIntervalInMillis = checkinIntervalInMillis;
    }

    public long getAge() {
        return DateTime.now().getMillis() - checkinTimeInMillis;
    }

    public boolean isStale() {
        return new DateTime(checkinTimeInMillis).plusMillis(checkinIntervalInMillis).plusSeconds(10).isBeforeNow();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuartzScheduler that = (QuartzScheduler) o;

        if (schedulerName != null ? !schedulerName.equals(that.schedulerName) : that.schedulerName != null)
            return false;
        return instanceName != null ? instanceName.equals(that.instanceName) : that.instanceName == null;
    }

    @Override
    public int hashCode() {
        int result = schedulerName != null ? schedulerName.hashCode() : 0;
        result = 31 * result + (instanceName != null ? instanceName.hashCode() : 0);
        return result;
    }
}
