package uk.ac.warwick.util.virusscan.conf;

import uk.ac.warwick.util.core.StringUtils;

public interface Configuration {

    String API_HOST_PROPERTY = "virusscan.api.host";

    String API_KEY_PROPERTY = "virusscan.api.key";

    String DEFAULT_API_HOST = "https://virusscan.warwick.ac.uk";

    String getApiHost();

    String getApiKey();

    default void validate() throws IllegalArgumentException {
        if (!StringUtils.hasText(getApiHost())) {
            throw new IllegalArgumentException("Virus scan API host not set - please check your configuration");
        }

        if (!StringUtils.hasText(getApiKey())) {
            throw new IllegalArgumentException("Virus scan API key not set - please check your configuration");
        }
    }

}
