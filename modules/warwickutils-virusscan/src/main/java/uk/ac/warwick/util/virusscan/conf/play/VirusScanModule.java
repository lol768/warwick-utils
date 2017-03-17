package uk.ac.warwick.util.virusscan.conf.play;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import uk.ac.warwick.util.virusscan.VirusScanService;
import uk.ac.warwick.util.virusscan.conf.Configuration;
import uk.ac.warwick.util.virusscan.http.HttpVirusScanService;
import uk.ac.warwick.util.virusscan.http.AsyncHttpClient;
import uk.ac.warwick.util.virusscan.http.AsyncHttpClientImpl;

/**
 * A Guice module for a Play application that enables the HttpVirusScanService.
 * Note you don't need to enable this manually because it's enabled in reference.conf
 */
public class VirusScanModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Configuration.class).to(TypesafeConfiguration.class);
        bind(AsyncHttpClient.class).to(AsyncHttpClientImpl.class);
        bind(VirusScanService.class).to(HttpVirusScanService.class);
    }

    /**
     * depended by {@link TypesafeConfiguration}
     * @param playConfig matches if {@link Config} is named with "virusScanConfig"
     * @return {@link Config}
     */
    @Named("virusScanConfig")
    @Provides
    public Config provideConfigWithUnderlyingPlayConfig(play.api.Configuration playConfig) {
        return playConfig.underlying();
    }

}
