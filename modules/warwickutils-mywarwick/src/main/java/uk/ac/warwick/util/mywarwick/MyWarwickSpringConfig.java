package uk.ac.warwick.util.mywarwick;

import org.springframework.context.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Properties;

/**
 * this is a configuration bean for Spring (version >= 3) application
 * use it like this in context XML:
 * <pre>
 * {@code
 * <bean class="uk.ac.warwick.util.mywarwick.MyWarwickSpringConfig" />
 * }
 * </pre>
 *
 * a properties bean with id "applicationPropertiesForMyWarwick" is expected.
 * in most cases, there would already have been a properties bean, so alias it like this:
 * <pre>
 * {@code
 * <alias alias="applicationPropertiesForMyWarwick" name="yourPropertiesBeanId" />
 * }
 * </pre>
 */
@Configuration
@ComponentScan("uk.ac.warwick.util.mywarwick")
public class MyWarwickSpringConfig {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    @Named(value = "applicationPropertiesForMyWarwick")
    Properties propertiesBean;

    /**
     * this bean is depended by {@link uk.ac.warwick.util.mywarwick.MyWarwickServiceImpl}
     * @return {@link uk.ac.warwick.util.mywarwick.model.Configuration} which is implemented by {@link uk.ac.warwick.util.mywarwick.model.PropertiesConfiguration}
     * @throws IOException when properties cannot be loaded from context
     */
    @Bean
    public uk.ac.warwick.util.mywarwick.model.Configuration myWarwickServiceConfiguration() throws IOException {
        return new uk.ac.warwick.util.mywarwick.model.PropertiesConfiguration(propertiesBean);
    }
}
