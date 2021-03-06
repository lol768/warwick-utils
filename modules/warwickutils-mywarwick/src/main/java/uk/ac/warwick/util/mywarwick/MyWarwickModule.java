package uk.ac.warwick.util.mywarwick;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import uk.ac.warwick.util.mywarwick.model.Configuration;
import uk.ac.warwick.util.mywarwick.model.TypesafeConfiguration;
import uk.ac.warwick.util.mywarwick.model.request.ActivityFactory;
import uk.ac.warwick.util.mywarwick.model.request.ActivityFactoryImpl;

/**
 * this is a Guice module for Play Application.
 * to use this module, add the following to conf file:
 * <pre>
 * {@code
 * play.modules.enabled += uk.ac.warwick.util.mywarwick.MyWarwickModule
 * }
 * </pre>
 */
public class MyWarwickModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MyWarwickService.class).to(MyWarwickServiceImpl.class);
        bind(Configuration.class).to(TypesafeConfiguration.class);
        bind(HttpClient.class).to(HttpClientImpl.class);
        bind(ActivityFactory.class).to(ActivityFactoryImpl.class);
    }

    /**
     * depended by {@link uk.ac.warwick.util.mywarwick.model.TypesafeConfiguration}
     * @param playConfig matches if {@link com.typesafe.config.Config} is named with "myWarwickConfig"
     * @return {@link com.typesafe.config.Config}
     */
    @Named("myWarwickConfig")
    @Provides
    public Config provideConfigWithUnderlyingPlayConfig(play.api.Configuration playConfig) {
        return playConfig.underlying();
    }

}
