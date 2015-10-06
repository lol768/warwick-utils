package uk.ac.warwick.util.core.spring.scheduling;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

import java.text.ParseException;

/**
 * Needed to set Quartz useProperties=true when using Spring classes,
 * because Spring sets an object reference on JobDataMap that is not a String
 *
 * @link http://site.trimplement.com/using-spring-and-quartz-with-jobstore-properties/
 * @link http://forum.springsource.org/showthread.php?130984-Quartz-error-IOException
 */
public class PersistableCronTriggerFactoryBean extends CronTriggerFactoryBean {

    @Override
    public void afterPropertiesSet() throws ParseException {
        super.afterPropertiesSet();

        // Remove the JobDetail element
        getJobDataMap().remove("jobDetail");
    }

}
