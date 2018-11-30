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

    @Inject
    public TypesafeConfiguration(@Named("virusScanConfig") Config typeSafeConfig) {
       this.config = typeSafeConfig;
    }

    @Override
    public String getApiHost() {
        return config.getString(API_HOST_PROPERTY);
    }

    @Override
    public String getApiKey() {
        return config.getString(API_KEY_PROPERTY);
    }

    @PostConstruct
    public void init() {
        validate();
    }

}
