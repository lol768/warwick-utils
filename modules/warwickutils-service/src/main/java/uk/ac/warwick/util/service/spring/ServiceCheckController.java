package uk.ac.warwick.util.service.spring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.warwick.util.service.ServiceHealthcheckProvider;
import uk.ac.warwick.util.service.ServiceMetric;
import uk.ac.warwick.util.service.ServiceMetricProvider;

import java.util.Map;

/**
 * Provides URLs for automated service checks to verify what state
 * this app is in. Things like load balancers might check the GTG
 * endpoint to decide whether it should be in the pool.
 * @link https://repo.elab.warwick.ac.uk/projects/SYSAD/repos/puppet3/browse/docs/APPS.md
 */
@Controller
public class ServiceCheckController implements Lifecycle {

    /** Spring should wire in all beans that extend ServiceHealthcheckProvider */
    @Autowired(required = false)
    private ServiceHealthcheckProvider[] healthcheckProviders = new ServiceHealthcheckProvider[0];

    /** Spring should wire in all beans that extend ServiceMetricProvider */
    @Autowired(required = false)
    private ServiceMetricProvider[] metricProviders = new ServiceMetricProvider[0];

    private boolean running = false;

    @RequestMapping(value = "/service/gtg", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> gtg() {
        if (isRunning()) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>("\"OK\"", HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/service/healthcheck", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthcheck() throws Exception {
        if (isRunning()) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();
            json.put("success", true);

            ImmutableList.Builder<Map<String, Object>> healthchecks = ImmutableList.builder();
            for (ServiceHealthcheckProvider provider : healthcheckProviders) {
                healthchecks.add(provider.latest().asJson());
            }
            json.put("data", healthchecks.build());
            return new ResponseEntity<>(json.build(), HttpStatus.OK);
        }
    }

    /* https://repo.elab.warwick.ac.uk/projects/SYSAD/repos/puppet3/browse/docs/APPS.md */
    @RequestMapping(value = "/service/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> metrics() throws Exception {
        if (isRunning()) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();

            for (ServiceMetricProvider provider : metricProviders) {
                ServiceMetric<?> metric = provider.get();
                json.put(metric.getName(), metric.asJson());
            }

            return new ResponseEntity<>(json.build(), HttpStatus.OK);
        }
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
