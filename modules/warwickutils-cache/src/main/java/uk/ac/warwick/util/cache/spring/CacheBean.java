package uk.ac.warwick.util.cache.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryFactory;
import uk.ac.warwick.util.cache.Caches;

import java.io.Serializable;
import java.time.Duration;
import java.util.Properties;

public class CacheBean extends AbstractFactoryBean<Cache<?, ?>> {

    private String name;

    private CacheEntryFactory<?, ?> entryFactory;

    private Duration timeout;

    private Caches.CacheStrategy strategy = Caches.CacheStrategy.CaffeineIfAvailable;

    private Properties properties;

    @Override
    public Class<Cache> getObjectType() {
        return Cache.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Cache<?, ?> createInstance() throws Exception {
        Caches.Builder builder =
            Caches.builder(name, strategy)
                .entryFactory((CacheEntryFactory<Serializable, Serializable>) entryFactory);

        if (properties != null)
            builder = builder.properties(properties);

        if (timeout != null)
            builder = builder.expireAfterWrite(timeout);

        return builder.build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (name == null) {
            throw new IllegalStateException("Must specify a name for the cache");
        }

        if (entryFactory == null) {
            throw new IllegalStateException("Must specify a cache entry factory");
        }

        if (timeout == null) {
            throw new IllegalStateException("Must specify an entry timeout");
        }

        super.afterPropertiesSet();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEntryFactory(CacheEntryFactory<?, ?> entryFactory) {
        this.entryFactory = entryFactory;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void setStrategy(Caches.CacheStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategyAsString(String strategyAsString) {
        this.strategy = Caches.CacheStrategy.valueOf(strategyAsString);
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
