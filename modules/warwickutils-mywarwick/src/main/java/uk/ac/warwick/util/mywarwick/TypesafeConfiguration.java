package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TypesafeConfiguration implements Configuration {

    @Inject
    private com.typesafe.config.Config typeSafeConfigProperties;

    List<Instance> instanceList;

    private void initConfigList() {
        instanceList = typeSafeConfigProperties
                .getConfigList("mywarwick.services")
                .stream()
                .map(e -> new Instance(
                        e.getString("baseUrl"),
                        e.getString("providerId"),
                        e.getString("userName"),
                        e.getString("password")
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Instance> getInstances() {
        if (instanceList == null) instanceList = new ArrayList<>();
        if (instanceList.size() == 0) initConfigList();
        return instanceList;
    }

    @Override
    public void setInstances(List<Instance> instances) {
        this.instanceList = instances;
    }

    public void setTypeSafeConfigProperties(com.typesafe.config.Config typeSafeConfigProperties) {
        this.typeSafeConfigProperties = typeSafeConfigProperties;
    }
}
