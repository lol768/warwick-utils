package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.Config;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TypeSafeConfigs implements uk.ac.warwick.util.mywarwick.model.Configs {

    @Inject
    private com.typesafe.config.Config typeSafeConfigProperties;

    List<Config> configList;

    private void initConfigList() {
        configList = typeSafeConfigProperties
                .getConfigList("mywarwick.services")
                .stream()
                .map(e -> new Config(
                        e.getString("baseUrl"),
                        e.getString("providerId"),
                        e.getString("userName"),
                        e.getString("password")
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<uk.ac.warwick.util.mywarwick.model.Config> getConfigs() {
        if (configList == null) configList = new ArrayList<>();
        if (configList.size() == 0) initConfigList();
        return configList;
    }

    @Override
    public void setConfigs(List<uk.ac.warwick.util.mywarwick.model.Config> configs) {
        this.configList = configs;
    }

    public void setTypeSafeConfigProperties(com.typesafe.config.Config typeSafeConfigProperties) {
        this.typeSafeConfigProperties = typeSafeConfigProperties;
    }
}
