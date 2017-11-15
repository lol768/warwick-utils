package uk.ac.warwick.util.mywarwick.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class TypesafeConfiguration implements Configuration {

    private Config config;

    private Set<Instance> instanceSet;

    @Inject
    public TypesafeConfiguration(@Named("myWarwickConfig") com.typesafe.config.Config typeSafeConfig) {
       this.config = typeSafeConfig;
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

}
