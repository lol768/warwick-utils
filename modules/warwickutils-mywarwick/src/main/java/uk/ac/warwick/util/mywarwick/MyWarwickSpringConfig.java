package uk.ac.warwick.util.mywarwick;

import org.springframework.context.annotation.*;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan("uk.ac.warwick.util.mywarwick")
public class MyWarwickSpringConfig {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject @Named(value = "applicationPropertiesForMyWarwick")
    Properties propertiesBean;

    @Bean
    public uk.ac.warwick.util.mywarwick.model.Configuration configuration() throws IOException {
        return new uk.ac.warwick.util.mywarwick.model.PropertiesConfiguration(propertiesBean);
    }
}
