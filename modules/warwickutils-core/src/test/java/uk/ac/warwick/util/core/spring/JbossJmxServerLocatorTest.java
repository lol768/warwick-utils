package uk.ac.warwick.util.core.spring;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jmx.MBeanServerNotFoundException;

import uk.ac.warwick.util.core.spring.JbossJmxServerLocator;

import com.google.common.collect.Lists;


public class JbossJmxServerLocatorTest {
    private JbossJmxServerLocator locator;
    private Mockery m;
    @Before public void setup() throws Exception {
        m = new Mockery();
        locator = new JbossJmxServerLocator();
    }
    
    @Test public void jbossServerSecond() throws Exception {
        final MBeanServer jconsole = m.mock(MBeanServer.class, "jconsole");
        final MBeanServer jmx = m.mock(MBeanServer.class, "jboss-jmx");
        List<MBeanServer> servers = Lists.newArrayList(jconsole, jmx);
        
        m.checking(new Expectations(){{
            one(jconsole).getDefaultDomain(); will(returnValue("jconsole"));
            one(jmx).getDefaultDomain(); will(returnValue(JbossJmxServerLocator.JBOSS_DOMAIN));
        }});
        
        assertEquals(jmx, locator.choose(servers));
    }
    
    @Test public void pickFirstIfNoJboss() throws Exception {
        final MBeanServer jconsole = m.mock(MBeanServer.class, "jconsole");
        final MBeanServer johnsonsMbeanServer = m.mock(MBeanServer.class, "johnsons-mbean-server");
        List<MBeanServer> servers = Lists.newArrayList(jconsole, johnsonsMbeanServer);
        
        m.checking(new Expectations(){{
            one(jconsole).getDefaultDomain(); will(returnValue("jconsole"));
            one(johnsonsMbeanServer).getDefaultDomain(); will(returnValue("JOHNSON!"));
        }});
        
        assertEquals(jconsole, locator.choose(servers));
    }
    
    @Test(expected=MBeanServerNotFoundException.class) public void errorIfNoServers() throws Exception {
        locator.choose(new ArrayList<MBeanServer>());
    }
}
