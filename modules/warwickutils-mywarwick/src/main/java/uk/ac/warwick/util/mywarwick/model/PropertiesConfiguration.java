package uk.ac.warwick.util.mywarwick.model;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/** this implementation is for Spring applications **/
@Singleton
public class PropertiesConfiguration implements Configuration {
    private final Properties defaults;

    private Properties applicationProperties;

    private Set<Instance> instanceSet;

    @Inject
    public PropertiesConfiguration(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.defaults = Configuration.loadDefaults();
    }



    private String getOrDefault(String key) {
        return applicationProperties.getProperty(
                key,
                defaults.getProperty(key)
        );
    }

    private void initMyWarwickConfigs() {
        HashMap<Integer, String> configBaseUrls = new HashMap<>();
        HashMap<Integer, String> configProviderIds = new HashMap<>();
        HashMap<Integer, String> configUserNames = new HashMap<>();
        HashMap<Integer, String> configPasswords = new HashMap<>();
        HashMap<Integer, String> configLogErrors = new HashMap<>();
        applicationProperties
                .entrySet()
                .stream()
                .filter(e -> e.getKey().toString().contains("mywarwick.instances."))
                .forEach(element -> {
                    String key = element.getKey().toString();
                    Integer propertyIndex = Integer.valueOf(key.split("\\.")[2]);
                    if (key.contains("baseUrl")) configBaseUrls.put(propertyIndex, element.getValue().toString());
                    if (key.contains("providerId")) configProviderIds.put(propertyIndex, element.getValue().toString());
                    if (key.contains("userName")) configUserNames.put(propertyIndex, element.getValue().toString());
                    if (key.contains("password")) configPasswords.put(propertyIndex, element.getValue().toString());
                    if (key.contains("logErrors")) configLogErrors.put(propertyIndex, element.getValue().toString());
                });

        instanceSet = configBaseUrls.entrySet().stream().map(baseUrl -> {
            Integer index = baseUrl.getKey();
            return new Instance(
                    baseUrl.getValue(),
                    configProviderIds.get(index),
                    configUserNames.get(index),
                    configPasswords.get(index),
                    configLogErrors.get(index));
        }).collect(Collectors.toSet());
    }

    @Override
    public Set<Instance> getInstances() {
        if (instanceSet == null) {
            instanceSet = new HashSet<>();
            initMyWarwickConfigs();
        }
        return instanceSet;
    }

    @Override
    public int getHttpMaxConn() {
        return Integer.parseInt(getOrDefault("mywarwick.http.maxConn"));
    }

    @Override
    public int getHttpMaxConnPerRoute() {
        return Integer.parseInt(getOrDefault("mywarwick.http.maxConnPerRoute"));
    }
}
