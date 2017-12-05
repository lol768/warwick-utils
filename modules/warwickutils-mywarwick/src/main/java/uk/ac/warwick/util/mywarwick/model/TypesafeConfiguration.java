package uk.ac.warwick.util.mywarwick.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class TypesafeConfiguration implements Configuration {

    private Config config;

    private Set<Instance> instanceSet;

    @Inject
    public TypesafeConfiguration(@Named("myWarwickConfig") com.typesafe.config.Config typeSafeConfig) {
        Properties props = Configuration.loadDefaults();
        Config defaults = ConfigFactory.parseProperties(props);
        this.config = typeSafeConfig.withFallback(defaults);
    }

    private void initConfigList() {
        instanceSet = config
                .getConfigList("mywarwick.instances")
                .stream()
                .map(e -> {
                    String logErrors = null;
                    try {
                        logErrors = e.getString("logErrors");
                    } catch (ConfigException.Missing error) {
                        // Just leave null to get the default
                    }
                    return new Instance(
                            e.getString("baseUrl"),
                            e.getString("providerId"),
                            e.getString("userName"),
                            e.getString("password"),
                            logErrors
                    );
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> getInstances() {
        if (instanceSet == null) {
            instanceSet = new HashSet<>();
            initConfigList();
        }
        return instanceSet;
    }

    @Override
    public int getHttpMaxConn() {
        return config.getInt("mywarwick.http.maxConn");
    }

    @Override
    public int getHttpMaxConnPerRoute() {
        return config.getInt("mywarwick.http.maxConnPerRoute");
    }

}
