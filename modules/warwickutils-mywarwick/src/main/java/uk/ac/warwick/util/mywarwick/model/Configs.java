package uk.ac.warwick.util.mywarwick.model;

import javax.inject.Named;

@Named
public class Configs {

    private Config testConfig;
    private Config devConfig;
    private Config prodConfig;

    public Configs(Config testConfig, Config devConfig, Config prodConfig) {
        this.testConfig = testConfig;
        this.devConfig = devConfig;
        this.prodConfig = prodConfig;
    }

    public Config getTestConfig() {
        return testConfig;
    }

    public Config getDevConfig() {
        return devConfig;
    }

    public Config getProdConfig() {
        return prodConfig;
    }
}
