package uk.ac.warwick.util.mywarwick.model;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class TypesafeConfiguration implements Configuration {

    private com.typesafe.config.Config typeSafeConfigProperties;

    private Set<Instance> instanceSet;

    @Inject
    public TypesafeConfiguration(com.typesafe.config.Config typeSafeConfigPropertiest) {
       this.typeSafeConfigProperties = typeSafeConfigPropertiest;
    }

    private void initConfigList() {
        instanceSet = typeSafeConfigProperties
                .getConfigList("mywarwick.instances")
                .stream()
                .map(e -> new Instance(
                        e.getString("baseUrl"),
                        e.getString("providerId"),
                        e.getString("userName"),
                        e.getString("password")
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> getInstances() {
        if (instanceSet == null) instanceSet = new HashSet<>();
        if (instanceSet.size() == 0) initConfigList();
        return instanceSet;
    }

    @Override
    public void setInstances(Set<Instance> instances) {
        this.instanceSet = instances;
    }
}
