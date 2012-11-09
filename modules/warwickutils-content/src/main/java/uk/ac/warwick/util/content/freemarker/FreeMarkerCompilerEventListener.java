package uk.ac.warwick.util.content.freemarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import freemarker.template.Configuration;

/**
 * Implementation of ApplicationListener that will precompile all the freeMarker templates in the
 * specified resource directory.
 *
 * @author xusqac
 *
 * @todo This could be improved because it requires a copy of the Resource.  Unfortunately we cannot
 * simply ask the configuration because the TemplateLoader isn't clever enough to give us a list
 * of files :(
 * 
 * @requires Spring
 *
 */
public final class FreeMarkerCompilerEventListener implements ApplicationListener, ApplicationContextAware {
    private static final Logger LOGGER = Logger.getLogger(FreeMarkerCompilerEventListener.class);
    private final File templateDir;
    private final Configuration configuration;
    private ApplicationContext applicationContext;
    private String freemarkerNoPrecompileProperty;

    public FreeMarkerCompilerEventListener(final Configuration theConfiguration, final Resource theResource) {
        this.configuration = theConfiguration;
        this.templateDir = verifyResource(theResource);
    }

    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent
                && ((ContextRefreshedEvent)event).getApplicationContext().equals(applicationContext)) {
           compile();
       }
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

    }

    private File verifyResource(final Resource resource) {
        File file;
        try {
            file = resource.getFile();
        } catch (final IOException e) {
            throw new IllegalStateException("Resource " + resource + " must be a directory!", e);
        }

        if (!file.isDirectory()) {
            throw new IllegalStateException("Resource " + resource + " must be a directory!");
        }

        return file;
    }

    private void compile() {
        if ("true".equals(freemarkerNoPrecompileProperty)) return;
        
        
        String[] templateNames = getTemplateNames();
        for (String name: templateNames) {
            try {
                LOGGER.info("Precompiling freemarker template [" + name + "]");
                configuration.getTemplate(name);
            } catch (final IOException e) {
                throw new IllegalStateException("Cannot get template for " + name, e);
            }
        }
    }

    private String[] getTemplateNames() {
        List<String> files = new ArrayList<String>();
        for (File file: templateDir.listFiles()) {
            findFreeMarkerTemplates(file, "", files);
        }
        return files.toArray(new String[] {});
    }

    private void findFreeMarkerTemplates(final File root, final String parentPath, final List<String> filesFound) {
        if (root.isDirectory()) {
            // files need to be relative, i.e. links/enterLink.ftl *not* /links/enterLink.ftl
            String newParentPath = parentPath;
            if (StringUtils.hasLength(newParentPath)) {
                newParentPath += "/";
            }
            newParentPath += root.getName();

            for (File file: root.listFiles()) {
                findFreeMarkerTemplates(file, newParentPath, filesFound);
            }
        } else {
            if (root.getName().toLowerCase().endsWith(".ftl")) {
                filesFound.add(parentPath + "/" + root.getName());
            }
        }
    }
    
    public void setNoPrecompile(String noPrecompile) {
        this.freemarkerNoPrecompileProperty = noPrecompile;
    }
}
