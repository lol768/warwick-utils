package uk.ac.warwick.util.content.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

final class ConfigurationFactory {
	
	private static final Configuration configuration;
	
	static {
		configuration = new Configuration();
		
		configuration.setDefaultEncoding("ISO-8859-1");
		configuration.setOutputEncoding("ISO-8859-1");

		// Register default template loaders.
		TemplateLoader templateLoader = new ClassTemplateLoader(ConfigurationFactory.class, "/freemarker");
		configuration.setTemplateLoader(templateLoader);
	}
	
	public static Configuration getConfiguration() {
		return configuration;
	}

}
