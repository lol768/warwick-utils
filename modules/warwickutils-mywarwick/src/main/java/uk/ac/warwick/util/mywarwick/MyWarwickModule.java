package uk.ac.warwick.util.mywarwick;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import uk.ac.warwick.util.mywarwick.model.Configuration;
import uk.ac.warwick.util.mywarwick.model.TypesafeConfiguration;


public class MyWarwickModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MyWarwickService.class).to(MyWarwickServiceImpl.class);
        bind(Configuration.class).to(TypesafeConfiguration.class);
        bind(HttpClient.class).to(HttpClientImpl.class);
    }

    @Named("myWarwickConfig")
    @Provides
    public Config provideConfigWithUnderlyingPlayConfig(play.api.Configuration playConfig) {
        return playConfig.underlying();
    }

}
