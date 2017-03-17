package uk.ac.warwick.util.virusscan.conf.spring;

import org.springframework.beans.factory.annotation.Value;
import uk.ac.warwick.util.virusscan.conf.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class SpringConfiguration implements Configuration {

    private @Value("${" + API_HOST_PROPERTY + ":'" + DEFAULT_API_HOST + "'}") String apiHost;

    private @Value("${" + API_KEY_PROPERTY + "}") String apiKey;

    @Override
    public String getApiHost() {
        return apiHost;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @PostConstruct
    public void init() {
        validate();
    }
}
