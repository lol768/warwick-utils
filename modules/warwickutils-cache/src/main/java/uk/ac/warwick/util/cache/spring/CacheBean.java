package uk.ac.warwick.util.cache.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import uk.ac.warwick.util.cache.Cache;
import uk.ac.warwick.util.cache.CacheEntryFactory;
import uk.ac.warwick.util.cache.Caches;

public class CacheBean extends AbstractFactoryBean<Cache<?, ?>> {

    private String name;

    private CacheEntryFactory<?, ?> entryFactory;

    private Long timeout;

    private Caches.CacheStrategy strategy = Caches.CacheStrategy.EhCacheIfAvailable;

    @Override
    public Class<Cache> getObjectType() {
        return Cache.class;
    }

    @Override
    protected Cache<?, ?> createInstance() throws Exception {
        return Caches.newCache(name, entryFactory, timeout, strategy);
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

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setStrategy(Caches.CacheStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategyAsString(String strategyAsString) {
        this.strategy = Caches.CacheStrategy.valueOf(strategyAsString);
    }
}
