package uk.ac.warwick.util.content.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import uk.ac.warwick.util.core.StringUtils;

final class ConfigurationFactory {
	
	private static final Configuration configuration;
	
	static {
		configuration = new Configuration(Configuration.VERSION_2_3_21);
		
		configuration.setDefaultEncoding(StringUtils.DEFAULT_ENCODING);
		configuration.setOutputEncoding(StringUtils.DEFAULT_ENCODING);

		// Register default template loaders.
		TemplateLoader templateLoader = new ClassTemplateLoader(ConfigurationFactory.class, "/freemarker");
		configuration.setTemplateLoader(templateLoader);
	}
	
	public static Configuration getConfiguration() {
		return configuration;
	}

}
