package uk.ac.warwick.util.mywarwick;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import play.api.Configuration;

public class MyWarwickModuleTest {
    public static class TestModule extends AbstractModule {
        protected void configure() {
            // This emulates what Play does, providing a Configuration.
            bind(Configuration.class).toInstance(Configuration.apply(ConfigFactory.empty()));
        }
    }

    public static class EmptyConfigModule extends AbstractModule {
        protected void configure() {

        }

        // a rogue Config provider! Make sure it doesn't clash with the one in the module.
        @Provides
        public Config newConfig() {
            return ConfigFactory.empty();
        }
    }


    @Test
    public void noConflicts() {
        Injector injector = Guice.createInjector(new TestModule(), new MyWarwickModule());
    }

    @Test
    public void conflicts() {
        Injector injector = Guice.createInjector(new EmptyConfigModule(), new TestModule(), new MyWarwickModule());
    }

}