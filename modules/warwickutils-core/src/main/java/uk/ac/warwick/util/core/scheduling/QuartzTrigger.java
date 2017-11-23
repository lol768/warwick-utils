package uk.ac.warwick.util.core.scheduling;

import uk.ac.warwick.util.core.jodatime.DateTimeUtils;

import java.time.Instant;

public class QuartzTrigger {
    private String schedulerName;
    private String triggerName;
    private String jobName;
    private String state;
    private long startTimeInMillis;

    public QuartzTrigger() {}

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStartTimeInMillis(long startTimeInMillis) {
        this.startTimeInMillis = startTimeInMillis;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public String getJobName() {
        return jobName;
    }

    public String getState() {
        return state;
    }

    public long getAge() {
        return Instant.now(DateTimeUtils.CLOCK_IMPLEMENTATION).toEpochMilli() - startTimeInMillis;
    }

    public boolean isError() {
        return "ERROR".equals(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuartzTrigger that = (QuartzTrigger) o;

        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (schedulerName != null ? !schedulerName.equals(that.schedulerName) : that.schedulerName != null) return false;
        if (triggerName != null ? !triggerName.equals(that.triggerName) : that.triggerName != null) return false;
        return jobName != null ? jobName.equals(that.jobName) : that.jobName == null;
    }

    @Override
    public int hashCode() {
        int result = schedulerName != null ? schedulerName.hashCode() : 0;
        result = 31 * result + (triggerName != null ? triggerName.hashCode() : 0);
        result = 31 * result + (jobName != null ? jobName.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }
}
