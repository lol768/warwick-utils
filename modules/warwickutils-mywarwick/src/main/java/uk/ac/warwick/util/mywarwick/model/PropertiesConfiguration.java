package uk.ac.warwick.util.mywarwick.model;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class PropertiesConfiguration implements Configuration { //this implementation is for Spring applications


    private Properties applicationProperties;

    private Set<Instance> instanceSet;

    @Inject
    public PropertiesConfiguration(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

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

        instanceSet = configBaseUrls.entrySet().stream().map(baseUrl -> {
            Integer index = baseUrl.getKey();
            return new Instance(
                    baseUrl.getValue(),
                    configProviderIds.get(index),
                    configUserNames.get(index),
                    configPasswords.get(index));
        }).collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> getInstances() {
        if (instanceSet == null) instanceSet = new HashSet<>();
        if (instanceSet.size() == 0) initMyWarwickConfigs();
        return instanceSet;
    }

    @Override
    public void setInstances(Set<Instance> instances) {
        this.instanceSet = instances;
    }
}
