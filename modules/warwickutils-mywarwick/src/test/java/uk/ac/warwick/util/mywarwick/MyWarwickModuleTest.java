package uk.ac.warwick.util.mywarwick;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyWarwickModuleTest {

    @Test
    public void createModule() {
        Injector injector = Guice.createInjector(new TestModule(), new MyWarwickModule());
    }

    public static class TestModule extends AbstractModule {
        protected void configure() {
            bind(play.api.Configuration.class).toInstance(play.api.Configuration.apply(ConfigFactory.empty()));
        }
    }

}