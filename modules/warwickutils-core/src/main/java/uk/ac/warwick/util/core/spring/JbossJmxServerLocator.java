package uk.ac.warwick.util.core.spring;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.jmx.MBeanServerNotFoundException;

/**
 * FactoryBean that finds the MBeanServer we want to put our MBeans
 * into - this is the JBoss JMX console, which has a particular domain
 * we check for. To use, simply wire this bean to the "server" property
 * of the MBeanExporter bean.
 * <p>
 * If no JBoss JMX console could be found then it will use the first
 * MBeanServer found in the list (which is also Spring's default behaviour).
 */
public final class JbossJmxServerLocator extends AbstractFactoryBean<MBeanServer> {

    public static final String JBOSS_DOMAIN = "jboss";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JbossJmxServerLocator.class);
    
    @Override
    protected MBeanServer createInstance() throws Exception {        
        return choose(MBeanServerFactory.findMBeanServer(null));
    }

    MBeanServer choose(List<MBeanServer> allServers) throws MBeanServerNotFoundException {
        if (allServers.isEmpty()) {
            throw new MBeanServerNotFoundException("No MBeanServers in JVM");
        } 
        
        for (MBeanServer s : allServers) {
            if (s.getDefaultDomain().equals(JBOSS_DOMAIN)) {
               LOGGER.info("Found JBoss's MBeanServer");
               return s;
            }
        }
        LOGGER.warn("No JBoss MBeanServer found, using first in list (out of total of "+allServers.size()+")");
        return allServers.get(0);
    }

    @Override
    public Class<MBeanServer> getObjectType() {
        return MBeanServer.class;
    }

}
