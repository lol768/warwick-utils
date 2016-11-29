package uk.ac.warwick.util.mywarwick;

import uk.ac.warwick.util.mywarwick.model.Instance;
import uk.ac.warwick.util.mywarwick.model.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class PropertiesConfiguration implements Configuration { //this implementation is for Spring applications

    @Inject
    private Properties applicationProperties;

    private List<Instance> instanceList;

    private void initMyWarwickConfigs() {

        HashMap<Integer, String> configBaseUrls = new HashMap<>();
        HashMap<Integer, String> configProviderIds = new HashMap<>();
        HashMap<Integer, String> configUserNames = new HashMap<>();
        HashMap<Integer, String> configPasswords = new HashMap<>();
        applicationProperties
                .entrySet()
                .stream()
                .filter(e -> e.getKey().toString().contains("mywarwick.services."))
                .forEach(element -> {
                    String key = element.getKey().toString();
                    Integer propertyIndex = Integer.valueOf(key.split("\\.")[2]);
                    if (key.contains("baseUrl")) configBaseUrls.put(propertyIndex, element.getValue().toString());
                    if (key.contains("providerId")) configProviderIds.put(propertyIndex, element.getValue().toString());
                    if (key.contains("userName")) configUserNames.put(propertyIndex, element.getValue().toString());
                    if (key.contains("password")) configPasswords.put(propertyIndex, element.getValue().toString());
                });

        instanceList = configBaseUrls.entrySet().stream().map(baseUrl -> {
            Integer index = baseUrl.getKey();
            return new Instance(
                    baseUrl.getValue(),
                    configProviderIds.get(index),
                    configUserNames.get(index),
                    configPasswords.get(index));
        }).collect(Collectors.toList());
    }

    @Override
    public List<Instance> getInstances() {
        if (instanceList == null) instanceList = new ArrayList<>();
        if (instanceList.size() == 0) initMyWarwickConfigs();
        return instanceList;
    }

    @Override
    public void setInstances(List<Instance> instances) {
        this.instanceList = instances;
    }

    public void setApplicationProperties(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }
}
