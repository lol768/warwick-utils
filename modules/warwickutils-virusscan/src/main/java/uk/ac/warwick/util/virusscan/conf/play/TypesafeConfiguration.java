package uk.ac.warwick.util.virusscan.conf.play;

import com.typesafe.config.Config;
import uk.ac.warwick.util.virusscan.conf.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class TypesafeConfiguration implements Configuration {

    private final Config config;

    private String apiHost;

    private String apiKey;

    @Inject
    public TypesafeConfiguration(@Named("virusScanConfig") Config typeSafeConfig) {
       this.config = typeSafeConfig;
    }

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
        this.apiHost = config.getString(API_HOST_PROPERTY);
        this.apiKey = config.getString(API_KEY_PROPERTY);

        validate();
    }

}
