package uk.ac.warwick.util.files.impl;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.TransientApiMetadata;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.SwiftApiMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import uk.ac.warwick.util.files.FileStore;

import java.util.Collections;

public class BlobStoreContextFactoryBean extends AbstractFactoryBean<BlobStoreContext> implements EnvironmentAware {

    @Value("${objectstore.swift.endpoint}")
    private String endpoint;

    @Value("${objectstore.swift.username}")
    private String username;

    @Value("${objectstore.swift.password}")
    private String password;

    private Environment environment;

    @Override
    public Class<FileStore> getObjectType() {
        return FileStore.class;
    }

    @Override
    protected BlobStoreContext createInstance() throws Exception {
        if (environment.acceptsProfiles("test")) {
            return
                ContextBuilder.newBuilder(new TransientApiMetadata())
                    .modules(Collections.singleton(new SLF4JLoggingModule()))
                    .buildView(BlobStoreContext.class);
        } else {
            return
                ContextBuilder.newBuilder(new SwiftApiMetadata())
                    .endpoint(endpoint)
                    .credentials(String.format("LDAP_%s:%s", username, username), password)
                    .modules(Collections.singleton(new SLF4JLoggingModule()))
                    .buildView(BlobStoreContext.class);
        }
    }

    @Override
    protected void destroyInstance(BlobStoreContext context) throws Exception {
        context.close();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
