package uk.ac.warwick.util.hibernate4.spring;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import uk.ac.warwick.util.core.scheduling.QuartzDAO;
import uk.ac.warwick.util.core.scheduling.QuartzScheduler;
import uk.ac.warwick.util.core.scheduling.QuartzTrigger;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static org.hibernate.type.StandardBasicTypes.*;

@Repository
public class HibernateQuartzDAO implements QuartzDAO {

    private SessionFactory sessionFactory;

    private Session getSession() {
        return getSessionFactory().getCurrentSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<QuartzTrigger> getPendingTriggers(String schedulerName) {
        return getSession()
            .createSQLQuery("select t.sched_name as schedulerName, " +
                "t.trigger_name as triggerName, " +
                "t.job_name as jobName, " +
                "t.start_time as startTimeInMillis, " +
                "t.trigger_state as state " +
                "from qrtz_triggers t " +
                "join qrtz_simple_triggers st on t.sched_name = st.sched_name and t.trigger_name = st.trigger_name " +
                "where t.trigger_state = 'WAITING' " +
                "and t.start_time < :now " +
                "and t.sched_name = :scheduler " +
                "and st.repeat_count = 0")
            .addScalar("schedulerName")
            .addScalar("triggerName")
            .addScalar("jobName")
            .addScalar("startTimeInMillis", LONG)
            .addScalar("state")
            .setResultTransformer(Transformers.aliasToBean(QuartzTrigger.class))
            .setString("scheduler", schedulerName)
            .setLong("now", System.currentTimeMillis())
            .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<QuartzScheduler> getSchedulers(String schedulerName) {
        return getSession()
            .createSQLQuery("select sched_name as schedulerName, " +
                "instance_name as instanceName, " +
                "last_checkin_time as checkinTimeInMillis, " +
                "checkin_interval as checkinIntervalInMillis " +
                "from qrtz_scheduler_state " +
                "where sched_name = :scheduler")
            .addScalar("schedulerName")
            .addScalar("instanceName")
            .addScalar("checkinTimeInMillis", LONG)
            .addScalar("checkinIntervalInMillis", INTEGER)
            .setResultTransformer(Transformers.aliasToBean(QuartzScheduler.class))
            .setString("scheduler", schedulerName)
            .list();
    }

    @Override
    public int countTriggerErrors(String schedulerName) {
        return ((Number) getSession()
            .createSQLQuery("select count(*) " +
                "from qrtz_triggers " +
                "where trigger_state = 'ERROR' " +
                "and sched_name = :scheduler")
            .setString("scheduler", schedulerName)
            .uniqueResult()).intValue();
    }

    public void setSessionFactory(SessionFactory sf) {
        sessionFactory = sf;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @PostConstruct
    public final void afterPropertiesSet() {
        if (sessionFactory == null) {
            throw new IllegalStateException("No sessionFactory set on " + getClass().getName());
        }
    }

}
