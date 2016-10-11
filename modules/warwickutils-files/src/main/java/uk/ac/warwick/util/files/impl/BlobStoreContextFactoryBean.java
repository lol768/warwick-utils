package uk.ac.warwick.util.files.impl;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.TransientApiMetadata;
import org.jclouds.filesystem.FilesystemApiMetadata;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.SwiftApiMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import uk.ac.warwick.util.core.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Properties;

public class BlobStoreContextFactoryBean extends AbstractFactoryBean<BlobStoreContext> {

    @Value("${objectstore.provider:swift}")
    private String providerType;

    @Value("${objectstore.swift.endpoint:}")
    private String swiftEndpoint;

    @Value("${objectstore.swift.username:}")
    private String swiftUsername;

    @Value("${objectstore.swift.password:}")
    private String swiftPassword;

    @Value("${objectstore.fileSystem.root:}")
    private String fileSystemRoot;

    @Override
    public Class<BlobStoreContext> getObjectType() {
        return BlobStoreContext.class;
    }

    @Override
    protected BlobStoreContext createInstance() throws Exception {
        switch (providerType) {
            case "inMemory":
                return
                    ContextBuilder.newBuilder(new TransientApiMetadata())
                        .modules(Collections.singleton(new SLF4JLoggingModule()))
                        .buildView(BlobStoreContext.class);
            case "fileSystem":
                Assert.isTrue(StringUtils.hasText(fileSystemRoot), "The property objectstore.fileSystem.root must be non-empty");
                File root = new File(fileSystemRoot);
                Assert.isTrue((root.exists() || root.mkdirs()) && root.isDirectory(), "The root directory " + root + " must exist and be a directory");

                return
                    ContextBuilder.newBuilder(new FilesystemApiMetadata())
                        .overrides(new Properties() {{
                            setProperty(FilesystemConstants.PROPERTY_BASEDIR, root.getAbsolutePath());
                        }})
                        .modules(Collections.singleton(new SLF4JLoggingModule()))
                        .buildView(BlobStoreContext.class);
            default:
                Assert.isTrue(StringUtils.hasText(swiftEndpoint), "The property objectstore.swift.endpoint must be non-empty");
                Assert.isTrue(StringUtils.hasText(swiftUsername), "The property objectstore.swift.username must be non-empty");
                Assert.isTrue(StringUtils.hasText(swiftPassword), "The property objectstore.swift.password must be non-empty");

                return
                    ContextBuilder.newBuilder(new SwiftApiMetadata())
                        .endpoint(swiftEndpoint)
                        .credentials(String.format("LDAP_%s:%s", swiftUsername, swiftUsername), swiftPassword)
                        .modules(Collections.singleton(new SLF4JLoggingModule()))
                        .buildView(BlobStoreContext.class);
        }
    }

    @Override
    protected void destroyInstance(BlobStoreContext context) throws Exception {
        context.close();
    }

}
